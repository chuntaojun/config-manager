/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lessspring.org.service.cluster;

import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.error.RaftError;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotReader;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotWriter;
import com.lessspring.org.DiskUtils;
import com.lessspring.org.pojo.ClusterMeta;
import com.lessspring.org.raft.NodeManager;
import com.lessspring.org.raft.SnapshotOperate;
import com.lessspring.org.raft.utils.ServerStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipOutputStream;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component(value = "snapshotOperate")
public class H2SnapshotOperateImpl implements SnapshotOperate {

	private final ClusterMeta clusterMeta = new ClusterMeta("com.lessspring.org.db");

	private final String SNAPSHOT_DIR = "db";
	private final String SNAPSHOT_ARCHIVE = "db.zip";
	private final NodeManager nodeManager = NodeManager.getInstance();
	private final String[][] fileNames = new String[][] {
			{ "snapshot_config_info.csv", "config_info" },
			{ "snapshot_config_info_beta.csv", "config_info_beta" },
			{ "snapshot_user.csv", "user" }, { "snapshot_user_role.csv", "user_role" },
			{ "snapshot_namespace.csv", "namespace" },
			{ "snapshot_namespace_permissions.csv", "namespace_permissions" } };
	private final DataSource dataSource;
	private FluxSink<Tuple2<SnapshotWriter, Closure>> source;

	public H2SnapshotOperateImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@PostConstruct
	public void init() {
		Flux.create(
				(Consumer<FluxSink<Tuple2<SnapshotWriter, Closure>>>) fluxSink -> source = fluxSink)
				.subscribe(this::onSnapshotSave);
	}

	@Override
	public void onSnapshotSave(SnapshotWriter writer, Closure done) {
		source.next(Tuples.of(writer, done));
	}

	private void onSnapshotSave(Tuple2<SnapshotWriter, Closure> job) {
		final SnapshotWriter writer = job.getT1();
		final Closure done = job.getT2();
		try {
			final String writePath = writer.getPath();
			final String parentPath = Paths.get(writePath, SNAPSHOT_DIR).toString();
			final File file = new File(parentPath);
			FileUtils.deleteDirectory(file);
			FileUtils.forceMkdir(file);
			List<String> sqls = new ArrayList<>();
			for (String[] tableInfo : fileNames) {
				final String filePath = parentPath + File.separator + tableInfo[0];
				String sql = "CALL CSVWRITE('%s', 'SELECT * FROM %s');";
				sql = String.format(sql, filePath, tableInfo[1]);
				sqls.add(sql);
			}
			batchExec(sqls, "Snapshot save");
			final String outputFile = Paths.get(writePath, SNAPSHOT_ARCHIVE).toString();
			try (final FileOutputStream fOut = new FileOutputStream(outputFile);
					final ZipOutputStream zOut = new ZipOutputStream(fOut)) {
				WritableByteChannel channel = Channels.newChannel(zOut);
				DiskUtils.compressDirectoryToZipFile(writePath, SNAPSHOT_DIR, zOut,
						channel);
				FileUtils.deleteDirectory(file);
			}
			if (writer.addFile(SNAPSHOT_ARCHIVE, buildMetadata(clusterMeta))) {
				done.run(Status.OK());
			}
			else {
				done.run(new Status(RaftError.EIO, "Fail to add snapshot file: %s",
						parentPath));
			}
		}
		catch (final Throwable t) {
			log.error("Fail to compress snapshot, path={}, file list={}, {}.",
					writer.getPath(), writer.listFiles(), t);
			done.run(new Status(RaftError.EIO,
					"Fail to compress snapshot at %s, error is %s", writer.getPath(),
					t.getMessage()));
		}
	}

	@Override
	public boolean onSnapshotLoad(SnapshotReader reader) {
		nodeManager.getSelf().setServerStatus(ServerStatus.ONLY_READ);
		final String readerPath = reader.getPath();
		final String sourceFile = Paths.get(readerPath, SNAPSHOT_ARCHIVE).toString();
		try {
			DiskUtils.unzipFile(sourceFile, readerPath);
			final String loadPath = Paths.get(readerPath, SNAPSHOT_DIR).toString()
					+ File.separator;
			log.info("snapshot load from : {}", loadPath);
			List<String> sqls = new ArrayList<>();
			for (String[] tableInfo : fileNames) {
				String file = loadPath + tableInfo[0];
				String sql = "CREATE TABLE IF NOT EXISTS CONFIG_MANAGER.%s AS SELECT * FROM CSVREAD('%s');";
				sql = String.format(sql, tableInfo[1], file);
				sqls.add(sql);
			}
			return batchExec(sqls, "Snapshot load");
		}
		catch (final Throwable t) {
			log.error("Fail to load snapshot, path={}, file list={}, {}.", readerPath,
					reader.listFiles(), t);
			return false;
		}
		finally {
			nodeManager.getSelf().setServerStatus(ServerStatus.HEALTH);
		}
	}

	private boolean batchExec(List<String> sqls, String type) {
		String sql = "";
		try (Connection connection = dataSource.getConnection()) {
			Statement statement = connection.createStatement();
			for (String t : sqls) {
				sql = t;
				log.info("snapshot load exec sql : {}", sql);
				statement.execute(sql);
			}
			connection.commit();
			return true;
		}
		catch (Exception e) {
			log.error(type + " exec sql : {} has some error : {}", sql, e);
			return false;
		}
	}

}
