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
import com.google.common.eventbus.Subscribe;
import com.lessspring.org.event.EventType;
import com.lessspring.org.event.ServerNodeChangeEvent;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.server.pojo.request.NodeChangeRequest;
import com.lessspring.org.raft.ClusterServer;
import com.lessspring.org.raft.NodeManager;
import com.lessspring.org.raft.SnapshotOperate;
import com.lessspring.org.raft.TransactionIdManager;
import com.lessspring.org.raft.conf.RaftServerOptions;
import com.lessspring.org.raft.pojo.Datum;
import com.lessspring.org.raft.vo.ServerNode;
import com.lessspring.org.server.service.distributed.BaseTransactionCommitCallback;
import com.lessspring.org.server.utils.PathConstants;
import com.lessspring.org.server.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@SuppressWarnings("all")
@Slf4j
public class ClusterManager {

	private final EventBus eventBus = new EventBus("ClusterManager-EventBus");
	private final NodeManager nodeManager = NodeManager.getInstance();
	private final SnapshotOperate snapshotOperate;
	private final List<BaseTransactionCommitCallback> commitCallbacks;
	private final AtomicBoolean initialize = new AtomicBoolean(false);
	private final TransactionIdManager transactionIdManager;
	private final PathConstants pathConstants;
	private ClusterServer clusterServer;

	public ClusterManager(List<BaseTransactionCommitCallback> commitCallbacks,
			SnapshotOperate snapshotOperate, TransactionIdManager transactionIdManager,
			PathConstants pathConstants) {
		this.commitCallbacks = commitCallbacks;
		this.snapshotOperate = snapshotOperate;
		this.transactionIdManager = transactionIdManager;
		this.pathConstants = pathConstants;
	}

	@PostConstruct
	public void init() {
		if (initialize.compareAndSet(false, true)) {
			final String raftCacheDir = Paths.get(pathConstants.getParentPath(), "raft-data")
					.toString();
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
			clusterServer.registerSnapshotOperator(snapshotOperate);
			clusterServer.initTransactionIdManger(transactionIdManager);
			clusterServer.init();
			transactionIdManager.init(0);
			eventBus.register(this);
			eventBus.register(clusterServer);
		}
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

	public ResponseData<List<ServerNode>> listNodes() {
		List<ServerNode> nodes = new ArrayList<>(nodeManager.serverNodes());
		return ResponseData.builder().withCode(200).withData(nodes).build();
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
		eventBus.post(event);
	}

	@Subscribe
	public void onChange(ServerNodeChangeEvent event) {
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