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
package com.conf.org.raft;

import com.alipay.remoting.InvokeCallback;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.sofa.jraft.entity.Task;
import com.conf.org.LifeCycle;
import com.conf.org.constant.Code;
import com.conf.org.event.EventType;
import com.conf.org.event.ServerNodeChangeEvent;
import com.conf.org.model.vo.ResponseData;
import com.conf.org.observer.Occurrence;
import com.conf.org.observer.Publisher;
import com.conf.org.observer.Watcher;
import com.conf.org.raft.conf.RaftServerOptions;
import com.conf.org.raft.pojo.Datum;
import com.conf.org.raft.pojo.Response;
import com.conf.org.raft.pojo.ServerNode;
import com.conf.org.SerializerUtils;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@SuppressWarnings("unchecked")
public class ClusterServer implements Watcher<ServerNodeChangeEvent>, LifeCycle {

	private static final String SERVER_NODE_SELF_INDEX = "cluster.node.self.index";
	private static final String SERVER_NODE_IP = "cluster.node.ip.";
	private static final String SERVER_NODE_RAFT_PORT = "cluster.node.raft.port.";
	private AtomicBoolean initialize = new AtomicBoolean(false);
	private RaftServer raftServer;
	private TransactionIdManager transactionIdManager;

	public ClusterServer(RaftServerOptions raftServerOptions) {
		raftServerOptions = Objects.isNull(raftServerOptions) ? new RaftServerOptions()
				: raftServerOptions;
		raftServer = new RaftServer(raftServerOptions);
		raftServer.init();
	}

	@Override
	public void init() {
		if (initialize.compareAndSet(false, true)) {
			raftServer.initRaftCluster();
			DatumAsyncUserProcessor processor = new DatumAsyncUserProcessor();
			raftServer.registerAsyncUserProcessor(processor);
		}
	}

	public void registerAsyncUserProcessor(BaseAsyncUserProcessor<Datum> processor) {
		processor.initCluster(this);
		raftServer.registerAsyncUserProcessor(processor);
	}

	public void registerTransactionCommitCallback(
			TransactionCommitCallback commitCallback) {
		raftServer.registerTransactionCommitCallback(commitCallback);
	}

	public void initTransactionIdManger(TransactionIdManager transactionIdManager) {
		this.transactionIdManager = transactionIdManager;
		this.raftServer.initTransactionIdManager(transactionIdManager);
	}

	public void registerSnapshotOperator(SnapshotOperate... snapshotOperate) {
		raftServer.registerSnapshotOperator(snapshotOperate);
	}

	public void rede(Datum datum) {

	}

	public CompletableFuture<ResponseData<Boolean>> apply(Datum datum)
			throws RemotingException, InterruptedException {
		needInitialized();
		final Throwable[] throwables = new Throwable[] { null };
		CompletableFuture<ResponseData<Boolean>> future = new CompletableFuture<>();
		if (raftServer.isLeader()) {
			final Task task = new Task();
			task.setDone(new ConfigStoreClosure(datum, status -> {
				ResponseData<Boolean> data = ResponseData.builder()
						.withCode(status.getCode()).withData(status.isOk())
						.withErrMsg(status.getErrorMsg()).build();
				future.complete(data);
				if (!status.isOk()) {
					throwables[0] = new RuntimeException(status.getErrorMsg());
				}
			}));
			task.setData(ByteBuffer.wrap(SerializerUtils.getInstance().serialize(datum)));
			raftServer.getNode().apply(task);
		}
		else {
			raftServer.getCliClientService().getRpcClient().invokeWithCallback(
					raftServer.leaderIp(), datum, new InvokeCallback() {
						@Override
						public void onResponse(Object o) {
							Response response = (Response) o;
							ResponseData<Boolean> data = ResponseData.builder()
									.withCode(Code.SUCCESS.getCode())
									.withData(response.isSuccess())
									.withErrMsg(response.getErrMsg()).build();
							future.complete(data);
						}

						@Override
						public void onException(Throwable throwable) {
							throwables[0] = throwable;
						}

						@Override
						public Executor getExecutor() {
							return null;
						}
					}, 5000);
		}
		if (Objects.nonNull(throwables[0])) {
			throw new RemotingException("Commit Error", throwables[0]);
		}
		return future;
	}

	public boolean isLeader() {
		needInitialized();
		return raftServer.isLeader();
	}

	@Override
	public void destroy() {
		raftServer.destroy();
	}

	@Override
	public boolean isInited() {
		return initialize.get();
	}

	@Override
	public boolean isDestroyed() {
		return false;
	}

	private void needInitialized() {
		if (!initialize.get()) {
			throw new IllegalStateException("Uninitialized cluster");
		}
	}

	private static void initClusterNode(NodeManager nodeManager, Properties properties) {
		int nodes = properties.size() / 2;
		int selfIndex = Integer
				.parseInt(properties.getProperty(SERVER_NODE_SELF_INDEX, "0"));
		for (int i = 0; i < nodes; i++) {
			String[] ip = properties.getProperty(SERVER_NODE_IP + i).split(":");
			ServerNode node = ServerNode.builder().nodeIp(ip[0]).port(Integer.parseInt(ip[1]))
					.build();
			if (i == selfIndex) {
				nodeManager.setSelf(node);
			}
			nodeManager.nodeJoin(node);
		}
	}

	@Override
	public void onNotify(Occurrence<ServerNodeChangeEvent> occurrence, Publisher publisher) {
		ServerNodeChangeEvent nodeChangeEvent = occurrence.getOrigin();
		needInitialized();
		EventType type = nodeChangeEvent.getType();
		ServerNode serverNode = ServerNode.builder().nodeIp(nodeChangeEvent.getNodeIp())
				.port(nodeChangeEvent.getNodePort()).build();
		if (EventType.PUBLISH.equals(type)) {
			raftServer.addNode(serverNode);
		}
		else {
			raftServer.removeNode(serverNode);
		}
	}
}