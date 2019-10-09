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
import com.alipay.sofa.jraft.entity.LocalFileMetaOutter;
import com.alipay.sofa.jraft.error.RaftError;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotReader;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotWriter;
import com.google.protobuf.ByteString;
import com.lessspring.org.DiskUtils;
import com.lessspring.org.pojo.ClusterMeta;
import com.lessspring.org.raft.SnapshotOperate;
import com.lessspring.org.repository.SnapshotMapper;
import com.lessspring.org.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.ZipOutputStream;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component(value = "snapshotOperate")
public class SnapshotOperateImpl implements SnapshotOperate {

    private final ClusterMeta clusterMeta = new ClusterMeta("com.lessspring.org.db");

    private final String SNAPSHOT_DIR = "db";
    private final String SNAPSHOT_ARCHIVE = "db.zip";

    private Executor executor;

    @Resource
    private SnapshotMapper snapshotMapper;

    @PostConstruct
    public void init() {
        executor = Executors.newFixedThreadPool(1, r -> {
            Thread thread = new Thread(r);
            thread.setName("com.lessspring.org.Raft.doSnapshot");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public void onSnapshotSave(SnapshotWriter writer, Closure done) {
        executor.execute(() -> {
            try {
                final String writePath = writer.getPath();
                final String parentPath = Paths.get(writePath, SNAPSHOT_DIR).toString();
                final File file = new File(parentPath);
                FileUtils.deleteDirectory(file);
                FileUtils.forceMkdir(file);
                snapshotMapper.doSnapshotSave(parentPath + File.separator);
                final String outputFile = Paths.get(writePath, SNAPSHOT_ARCHIVE).toString();
                try (final FileOutputStream fOut = new FileOutputStream(outputFile);
                     final ZipOutputStream zOut = new ZipOutputStream(fOut)) {
                    DiskUtils.compressDirectoryToZipFile(writePath, SNAPSHOT_DIR, zOut);
                    fOut.getFD().sync();
                    FileUtils.deleteDirectory(file);
                }
                if (writer.addFile(SNAPSHOT_ARCHIVE, buildMetadata(clusterMeta))) {
                    done.run(Status.OK());
                } else {
                    done.run(new Status(RaftError.EIO, "Fail to add snapshot file: %s", parentPath));
                }
            } catch (final Throwable t) {
                log.error("Fail to compress snapshot, path={}, file list={}, {}.", writer.getPath(), writer.listFiles(), t);
                done.run(new Status(RaftError.EIO, "Fail to compress snapshot at %s, error is %s", writer.getPath(), t
                        .getMessage()));
            }
        });
    }

    @Override
    public boolean onSnapshotLoad(SnapshotReader reader) {
        final String readerPath = reader.getPath();
        final String sourceFile = Paths.get(readerPath, SNAPSHOT_ARCHIVE).toString();
        try {
            DiskUtils.unzipFile(sourceFile, readerPath);
            snapshotMapper.doSnapshotLoad(sourceFile);
            return true;
        } catch (final Throwable t) {
            log.error("Fail to load snapshot, path={}, file list={}, {}.", readerPath, reader.listFiles(), t);
            return false;
        }
    }

    private <T> LocalFileMetaOutter.LocalFileMeta buildMetadata(final T metadata) {
        return metadata == null ? null : LocalFileMetaOutter.LocalFileMeta.newBuilder()
                .setUserMeta(ByteString.copyFrom(GsonUtils.toJsonBytes(metadata)))
                .build();
    }

}
