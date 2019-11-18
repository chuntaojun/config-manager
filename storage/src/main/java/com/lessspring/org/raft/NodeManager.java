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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.alipay.sofa.jraft.entity.PeerId;
import com.lessspring.org.raft.vo.ServerNode;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class NodeManager implements LeaderStatusListener {

	private ServerNode self = null;

	private Set<NodeChangeListener> listeners = new LinkedHashSet<>();

	private Map<String, ServerNode> nodeMap = new ConcurrentHashMap<>(3);

	private static volatile NodeManager INSTANCE = new NodeManager();

	public static NodeManager getInstance() {
		return INSTANCE;
	}

	public synchronized void registerListener(NodeChangeListener listener) {
		listeners.add(listener);
	}

	public ServerNode getSelf() {
		return self;
	}

	public void setSelf(ServerNode self) {
		this.self = self;
	}

	public synchronized void nodeJoin(ServerNode node) {
		nodeMap.putIfAbsent(node.getKey(), node);
		notifyListener();
	}

	public synchronized void nodeLeave(ServerNode node) {
		nodeMap.remove(node.getKey());
		notifyListener();
	}

	synchronized void batchUpdate(List<PeerId> peerIds, PeerId leader) {
		nodeMap.clear();
		peerIds.forEach(peerId -> {
			ServerNode serverNode = ServerNode.builder().nodeIp(peerId.getIp())
					.port(peerId.getPort()).build();
			if (Objects.equals(leader.checksum(), peerId.checksum())) {
				serverNode.setRole("Leader");
			}
			else {
				serverNode.setRole("Follower");
			}
			nodeMap.put(serverNode.getKey(), serverNode);
		});
		notifyListener();
	}

	public Stream<Map.Entry<String, ServerNode>> stream() {
		return new HashMap<>(nodeMap).entrySet().stream();
	}

	public synchronized Collection<ServerNode> serverNodes() {
		return new HashMap<>(nodeMap).values();
	}

	private void notifyListener() {
		Collection<ServerNode> nodes = nodeMap.values();
		for (NodeChangeListener listener : listeners) {
			listener.onChange(nodes);
		}
	}

	@Override
	public void onLeaderStart(String leaderIp, long term) {
		nodeMap.forEach((s, serverNode) -> {
			if (Objects.equals(leaderIp, serverNode.getKey())) {
				serverNode.setRole("Leader");
			}
			else {
				serverNode.setRole("Follower");
			}
		});
	}

	@Override
	public void onLeaderStop(String leaderIp, long term) {
		nodeMap.forEach((s, serverNode) -> {
			if (Objects.equals(leaderIp, serverNode.getKey())) {
				serverNode.setRole("Leader");
			}
			else {
				serverNode.setRole("Follower");
			}
		});
	}
}
