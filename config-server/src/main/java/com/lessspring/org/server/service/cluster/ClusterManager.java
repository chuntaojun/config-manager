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
package com.lessspring.org.server.service.cluster;

import com.google.common.eventbus.EventBus;
import com.lessspring.org.event.EventType;
import com.lessspring.org.event.ServerNodeChangeEvent;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.observer.Occurrence;
import com.lessspring.org.observer.Publisher;
import com.lessspring.org.observer.Watcher;
import com.lessspring.org.raft.ClusterServer;
import com.lessspring.org.raft.NodeManager;
import com.lessspring.org.raft.SnapshotOperate;
import com.lessspring.org.raft.TransactionIdManager;
import com.lessspring.org.raft.conf.RaftServerOptions;
import com.lessspring.org.raft.pojo.Datum;
import com.lessspring.org.raft.pojo.ServerNode;
import com.lessspring.org.raft.pojo.TransactionId;
import com.lessspring.org.raft.vo.ServerNodeVO;
import com.lessspring.org.server.pojo.request.NodeChangeRequest;
import com.lessspring.org.server.service.distributed.BaseTransactionCommitCallback;
import com.lessspring.org.server.utils.BzConstants;
import com.lessspring.org.server.utils.PathConstants;
import com.lessspring.org.server.utils.SpringUtils;
import com.lessspring.org.server.utils.VOUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@SuppressWarnings("all")
@Slf4j
public class ClusterManager extends Publisher<ServerNodeChangeEvent>
		implements Watcher<ServerNodeChangeEvent> {

	private final EventBus eventBus = new EventBus("ClusterManager-EventBus");
	private final NodeManager nodeManager = NodeManager.getInstance();
	private final List<SnapshotOperate> snapshotOperates;
	private final List<BaseTransactionCommitCallback> commitCallbacks;
	private final AtomicBoolean initialize = new AtomicBoolean(false);
	private final TransactionIdManager transactionIdManager;
	private final PathConstants pathConstants;
	private ClusterServer clusterServer;

	public ClusterManager(List<BaseTransactionCommitCallback> commitCallbacks,
			List<SnapshotOperate> snapshotOperates,
			TransactionIdManager transactionIdManager, PathConstants pathConstants) {
		this.commitCallbacks = commitCallbacks;
		this.snapshotOperates = snapshotOperates;
		this.transactionIdManager = transactionIdManager;
		this.pathConstants = pathConstants;
	}

	@PostConstruct
	public void init() {
		if (initialize.compareAndSet(false, true)) {
			final String raftCacheDir = Paths
					.get(pathConstants.getParentPath(), "raft-data").toString();
			final RaftServerOptions configuration = RaftServerOptions.builder()
					.cacheDir(raftCacheDir)
					.electionTimeoutMs(SpringUtils.getEnvironment().getProperty(
							"com.lessspring.org.config-manager.raft.electionTimeoutMs",
							Integer.class, 1000))
					.snapshotIntervalSecs(SpringUtils.getEnvironment().getProperty(
							"com.lessspring.org.config-manager.raft.snapshotIntervalSecs",
							Integer.class, 600))
					.build();
			clusterServer = new ClusterServer(configuration);
			for (BaseTransactionCommitCallback commitCallback : commitCallbacks) {
				clusterServer.registerTransactionCommitCallback(commitCallback);
			}
			clusterServer.registerSnapshotOperator(
					snapshotOperates.toArray(new SnapshotOperate[0]));
			clusterServer.initTransactionIdManger(transactionIdManager);
			clusterServer.init();
			registerTransactionId(transactionIdManager);
			registerWatcher(this::onNotify);
			registerWatcher(clusterServer);
		}
	}

	private void registerTransactionId(TransactionIdManager manager) {
		manager.register(new TransactionId(BzConstants.CONFIG_INFO));
		manager.register(new TransactionId(BzConstants.CONFIG_INFO_BETA));
		manager.register(new TransactionId(BzConstants.CONFIG_INFO_HISTORY));
		manager.register(new TransactionId(BzConstants.USER_ID));
		manager.init();
	}

	public void destroy() {
		clusterServer.destroy();
	}

	public boolean isLeader() {
		return clusterServer.isLeader();
	}

	public TransactionIdManager getTransactionIdManager() {
		return transactionIdManager;
	}

	public Mono<?> nodeAdd(NodeChangeRequest request) {
		nodeChange(request, EventType.PUBLISH);
		return Mono.just(ResponseData.success());
	}

	public Mono<?> nodeRemove(NodeChangeRequest request) {
		nodeChange(request, EventType.DELETE);
		return Mono.just(ResponseData.success());
	}

	private void nodeChange(NodeChangeRequest request, EventType type) {
		ServerNodeChangeEvent event = ServerNodeChangeEvent.builder()
				.nodeIp(request.getNodeIp()).nodePort(request.getNodePort()).type(type)
				.build();
		publishEvent(event);
	}

	public ResponseData<List<ServerNodeVO>> listNodes() {
		Collection<ServerNode> nodes = nodeManager.serverNodes();
		List<ServerNodeVO> vos = new ArrayList<>();
		long id = 0;
		for (ServerNode node : nodes) {
			vos.add(VOUtils.convertServerNodeVO(id++, node));
		}
		return ResponseData.builder().withCode(200).withData(vos).build();
	}

	public CompletableFuture<ResponseData<Boolean>> commit(Datum datum,
			final FailCallback failCallback) {
		try {
			return clusterServer.apply(datum);
		}
		catch (Exception e) {
			failCallback.onError(e);
			return CompletableFuture.completedFuture(ResponseData.fail());
		}
	}

	private void publishEvent(ServerNodeChangeEvent event) {
		notifyAllWatcher(event);
	}

	@Override
	public void onNotify(Occurrence<ServerNodeChangeEvent> occurrence,
			Publisher publisher) {
		ServerNodeChangeEvent event = occurrence.getOrigin();
		ServerNode node = ServerNode.builder().nodeIp(event.getNodeIp())
				.port(event.getNodePort()).build();
		switch (event.getType()) {
		case PUBLISH:
			nodeManager.nodeJoin(node);
			break;
		case DELETE:
			nodeManager.nodeLeave(node);
			break;
		default:
			throw new IllegalArgumentException(
					"Illegal cluster nodes transfer event type");
		}
	}
}
