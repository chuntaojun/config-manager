package com.lessspring.org.service.cluster;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;

import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.error.RaftError;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotReader;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotWriter;
import com.google.gson.reflect.TypeToken;
import com.lessspring.org.DiskUtils;
import com.lessspring.org.pojo.ClusterMeta;
import com.lessspring.org.raft.SnapshotOperate;
import com.lessspring.org.raft.TransactionIdManager;
import com.lessspring.org.raft.pojo.TransactionId;
import com.lessspring.org.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019-11-26 22:52
 */
@Slf4j
@Component
public class TransactionIdSnapshotOperateImpl implements SnapshotOperate {

	private final ClusterMeta clusterMeta = new ClusterMeta(
			"com.lessspring.org.transactionIdManager");

	private final String SNAPSHOT_DIR = "transactionIdManager";

	@Autowired
	private TransactionIdManager idManager;

	@Override
	public void onSnapshotSave(SnapshotWriter writer, Closure done) {
		final String writePath = writer.getPath();
		final String parentPath = Paths.get(writePath, SNAPSHOT_DIR).toString();
		final File file = new File(parentPath);
		try {
			FileUtils.deleteDirectory(file);
			FileUtils.forceMkdir(file);
			if (writer.addFile(SNAPSHOT_DIR, buildMetadata(clusterMeta))) {
				done.run(Status.OK());
			}
			else {
				done.run(new Status(RaftError.EIO, "Fail to add snapshot file: %s",
						parentPath));
			}
		}
		catch (Exception e) {
			log.error(
					"[TransactionIdSnapshotOperateImpl onSnapshotSave] has some error : {}",
					e);
		}
	}

	@Override
	public boolean onSnapshotLoad(SnapshotReader reader) {
		try {
			final String readerPath = reader.getPath();
			final String json = DiskUtils.readFile(readerPath, SNAPSHOT_DIR);
			Map<String, TransactionId> snapshot = GsonUtils.toObj(json,
					new TypeToken<Map<String, TransactionId>>() {
					});
			idManager.snapshotLoad(snapshot);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
}
