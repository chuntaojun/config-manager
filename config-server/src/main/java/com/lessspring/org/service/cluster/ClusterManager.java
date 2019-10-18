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

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.lessspring.org.event.EventType;
import com.lessspring.org.event.ServerNodeChangeEvent;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.pojo.request.NodeChangeRequest;
import com.lessspring.org.raft.ClusterServer;
import com.lessspring.org.raft.NodeManager;
import com.lessspring.org.raft.RaftConfiguration;
import com.lessspring.org.raft.SnapshotOperate;
import com.lessspring.org.raft.dto.Datum;
import com.lessspring.org.raft.vo.ServerNode;
import com.lessspring.org.service.distributed.BaseTransactionCommitCallback;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
public class ClusterManager {

	@Value("${com.lessspring.org.config-manager.raft.cacheDir:${user.home}/${server.port}}")
	private String raftCacheDir;

	@Value("${com.lessspring.org.config-manager.raft.electionTimeoutMs:1000}")
	private Integer electionTimeoutMs;

	@Value("${com.lessspring.org.config-manager.raft.snapshotIntervalSecs:600}")
	private Integer snapshotIntervalSecs;

	private final EventBus eventBus = new EventBus("ClusterManager-EventBus");
	private final NodeManager nodeManager = NodeManager.getInstance();
	private ClusterServer clusterServer;
	private final SnapshotOperate snapshotOperate;
	private final List<BaseTransactionCommitCallback> commitCallbacks;
	private final AtomicBoolean initialize = new AtomicBoolean(false);

	public ClusterManager(List<BaseTransactionCommitCallback> commitCallbacks,
			SnapshotOperate snapshotOperate) {
		this.commitCallbacks = commitCallbacks;
		this.snapshotOperate = snapshotOperate;
	}

	public void init() {
		if (initialize.compareAndSet(false, true)) {
			final RaftConfiguration configuration = RaftConfiguration.builder()
					.withCacheDir(raftCacheDir).withElectionTimeoutMs(electionTimeoutMs)
					.withSnapshotIntervalSecs(snapshotIntervalSecs).build();
			clusterServer = new ClusterServer(configuration);
			for (BaseTransactionCommitCallback commitCallback : commitCallbacks) {
				clusterServer.registerTransactionCommitCallback(commitCallback);
			}
			clusterServer.registerSnapshotOperator(snapshotOperate);
			clusterServer.init();
			eventBus.register(this);
			eventBus.register(clusterServer);
		}
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
		List<ServerNode> nodes = nodeManager.stream().map(Map.Entry::getValue)
				.collect(Collectors.toList());
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
			throw new IllegalArgumentException("Illegal cluster nodes change event type");
		}
	}

}
