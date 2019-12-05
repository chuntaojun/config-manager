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
package com.lessspring.org.raft;

import com.alipay.remoting.rpc.RpcServer;
import com.alipay.sofa.jraft.JRaftUtils;
import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.RaftGroupService;
import com.alipay.sofa.jraft.RouteTable;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.CliOptions;
import com.alipay.sofa.jraft.option.NodeOptions;
import com.alipay.sofa.jraft.rpc.CliRequests;
import com.alipay.sofa.jraft.rpc.RaftRpcServerFactory;
import com.alipay.sofa.jraft.rpc.RpcResponseClosure;
import com.alipay.sofa.jraft.rpc.impl.cli.BoltCliClientService;
import com.google.protobuf.Message;
import com.lessspring.org.LifeCycle;
import com.lessspring.org.executor.ThreadPoolHelper;
import com.lessspring.org.raft.conf.RaftServerOptions;
import com.lessspring.org.raft.machine.ConfigStateMachineAdapter;
import com.lessspring.org.raft.pojo.Datum;
import com.lessspring.org.raft.vo.ServerNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Requires user can customize the cluster parameters
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
class RaftServer implements LifeCycle {

	private String raftGroupId = "CONFIG_MANAGER";

	private RaftGroupService raftGroupService;
	private Node node;
	private Configuration conf;
	private ConfigStateMachineAdapter csm;
	private RpcServer rpcServer;
	private BoltCliClientService cliClientService;
	private ScheduledExecutorService scheduledExecutorService;
	private final NodeManager nodeManager = NodeManager.getInstance();
	private final RaftServerOptions raftServerOptions;
	private final AtomicBoolean inited = new AtomicBoolean(false);
	private final AtomicBoolean destroyed = new AtomicBoolean(false);

	RaftServer(RaftServerOptions raftServerOptions) {
		this.raftServerOptions = raftServerOptions;
	}

	void initRaftCluster() {
		String path = raftServerOptions.getCacheDir();
		try {
			FileUtils.forceMkdir(new File(path));
		}
		catch (IOException e) {
			log.error("Init File dir have some error : {}", e.getMessage());
			throw new RuntimeException(e);
		}
		String selfIp = nodeManager.getSelf().getNodeIp();
		int selfPort = nodeManager.getSelf().getPort() + 1000;
		final NodeOptions nodeOptions = new NodeOptions();
		// 设置选举超时时间为 5 秒
		nodeOptions.setElectionTimeoutMs(raftServerOptions.getElectionTimeoutMs());
		// 每隔600秒做一次 snapshot
		nodeOptions.setSnapshotIntervalSecs(raftServerOptions.getSnapshotIntervalSecs());
		// 设置初始集群配置
		nodeOptions.setInitialConf(conf);

		// 设置存储路径
		// 日志, 必须
		nodeOptions.setLogUri(path + File.separator + raftServerOptions.getLogUri());
		// 元信息, 必须
		nodeOptions.setRaftMetaUri(
				path + File.separator + raftServerOptions.getRaftMetaUri());
		// snapshot, 可选, 一般都推荐
		nodeOptions.setSnapshotUri(
				path + File.separator + raftServerOptions.getSnapshotUri());

		nodeOptions.setFsm(this.csm);

		// rpc port = server.port + 1000
		rpcServer = new RpcServer(selfPort, true, true);

		RaftRpcServerFactory.addRaftRequestProcessors(rpcServer);

		// 初始化 raft group 服务框架
		this.raftGroupService = new RaftGroupService(raftGroupId,
				JRaftUtils.getPeerId(selfIp + ":" + selfPort), nodeOptions, rpcServer);
		// 启动

		this.node = this.raftGroupService.start(raftServerOptions.isStartRpcServer());

		RouteTable.getInstance().updateConfiguration(raftGroupId, conf);

		this.cliClientService = new BoltCliClientService();
		cliClientService.init(new CliOptions());

		scheduledExecutorService.scheduleAtFixedRate(this::refresh, 3, 3,
				TimeUnit.MINUTES);
	}

	public Node getNode() {
		return node;
	}

	BoltCliClientService getCliClientService() {
		return cliClientService;
	}

	void initTransactionIdManager(TransactionIdManager manager) {
		csm.setTransactionIdManager(manager);
	}

	void registerAsyncUserProcessor(BaseAsyncUserProcessor<Datum> processor) {
		rpcServer.registerUserProcessor(processor);
	}

	void registerTransactionCommitCallback(TransactionCommitCallback commitCallback) {
		csm.registerTransactionCommitCallback(commitCallback);
	}

	void registerSnapshotOperator(SnapshotOperate snapshotOperate) {
		csm.registerSnapshotManager(snapshotOperate);
	}

	void addNode(ServerNode serverNode) {
		PeerId leader = leaderNode();
		final CliRequests.AddPeerRequest.Builder rb = CliRequests.AddPeerRequest
				.newBuilder();
		rb.setGroupId(raftGroupId);
		rb.setPeerId(PeerId.parsePeer(serverNode.getKey()).toString());
		cliClientService.addPeer(leader.getEndpoint(), rb.build(),
				new RpcResponseClosure<CliRequests.AddPeerResponse>() {
					@Override
					public void setResponse(CliRequests.AddPeerResponse resp) {
					}

					@Override
					public void run(Status status) {
						if (status.isOk()) {
							nodeManager.nodeJoin(serverNode);
						}
					}
				});

	}

	void removeNode(ServerNode serverNode) {
		PeerId leader = leaderNode();
		final CliRequests.RemovePeerRequest.Builder rb = CliRequests.RemovePeerRequest
				.newBuilder();
		rb.setGroupId(raftGroupId);
		rb.setPeerId(PeerId.parsePeer(serverNode.getKey()).toString());
		cliClientService.removePeer(leader.getEndpoint(), rb.build(),
				new RpcResponseClosure<CliRequests.RemovePeerResponse>() {
					@Override
					public void setResponse(CliRequests.RemovePeerResponse resp) {
					}

					@Override
					public void run(Status status) {
						if (status.isOk()) {
							nodeManager.nodeLeave(serverNode);
						}
					}
				});

	}

	private PeerId leaderNode() {
		if (node.getLeaderId() != null) {
			return node.getLeaderId();
		}
		final CliRequests.GetLeaderRequest.Builder rb = CliRequests.GetLeaderRequest
				.newBuilder();
		rb.setGroupId(raftGroupId);
		rb.setPeerId(node.getNodeId().getPeerId().toString());
		try {
			Message result = cliClientService
					.getLeader(node.getNodeId().getPeerId().getEndpoint(), rb.build(),
							null)
					.get(1000, TimeUnit.MILLISECONDS);
			if (result instanceof CliRequests.GetLeaderResponse) {
				CliRequests.GetLeaderResponse resp = (CliRequests.GetLeaderResponse) result;
				return JRaftUtils.getPeerId(resp.getLeaderId());
			}
		}
		catch (Exception e) {
			log.error("Get leader node has error : {}", e.getMessage());
		}
		return PeerId.parsePeer(nodeManager.getSelf().getKey());
	}

	String leaderIp() {
		PeerId leader = leaderNode();
		return leader.getIp() + ":" + leader.getPort();
	}

	boolean isLeader() {
		return csm.isLeader();
	}

	private void refresh() {
		int timeoutMs = 5000;
		try {
			if (!RouteTable.getInstance()
					.refreshLeader(cliClientService, raftGroupId, timeoutMs).isOk()) {
				log.warn("refresh raft node info failed");
			}
			nodeManager.batchUpdate(node.listAlivePeers(), node.getLeaderId());
		}
		catch (InterruptedException | TimeoutException e) {
			log.error("refresh raft node info failed, error is : {}", e.getMessage());
		}
	}

	@Override
	public void init() {
		if (inited.compareAndSet(false, true)) {
			scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r -> {
				Thread thread = new Thread(r);
				thread.setDaemon(true);
				thread.setName("config-manager-raft-node-refresh");
				return thread;
			});

			this.csm = new ConfigStateMachineAdapter();

			this.conf = new Configuration();
			nodeManager.stream().forEach(stringServerNodeEntry -> {
				ServerNode _node = stringServerNodeEntry.getValue();
				String address = _node.getNodeIp() + ":" + (_node.getPort() + 1000);
				PeerId peerId = JRaftUtils.getPeerId(address);
				conf.addPeer(peerId);
				com.alipay.sofa.jraft.NodeManager.getInstance()
						.addAddress(peerId.getEndpoint());
			});

			this.csm.registerLeaderStatusListener(nodeManager);
		}
	}

	@Override
	public void destroy() {
		if (isInited() && destroyed.compareAndSet(false, true)) {
			ThreadPoolHelper.invokeShutdown(scheduledExecutorService);
			raftGroupService.shutdown();
			cliClientService.shutdown();
			rpcServer.stop();
		}
	}

	@Override
	public boolean isInited() {
		return inited.get();
	}

	@Override
	public boolean isDestroyed() {
		return destroyed.get();
	}
}
