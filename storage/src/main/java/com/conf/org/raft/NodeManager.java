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

import com.alipay.sofa.jraft.entity.PeerId;
import com.conf.org.raft.pojo.ServerNode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
public class NodeManager implements LeaderStatusListener {

	private static volatile NodeManager INSTANCE = new NodeManager();

	private static final String SERVER_MODE = "cluster.server.mode.standalone";
	private static final String SERVER_NODE_SELF_INDEX = "cluster.server.node.self.index";
	private static final String SERVER_NODE_IP = "cluster.server.node.ip.";
	private static final String SERVER_NODE_PORT = "cluster.server.node.port.";

	static {
		NodeManager nodeManager = NodeManager.getInstance();
		try (InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("cluster.properties")) {
			Properties properties = new Properties();
			properties.load(is);
			initClusterNode(nodeManager, properties);
		}
		catch (IOException e) {
			log.error("Server");
			throw new RuntimeException(e);
		}
	}

	public static void load() {
	}

	private static void initClusterNode(NodeManager nodeManager, Properties properties) {
		boolean isStandalone = Boolean
				.parseBoolean(System.getProperty(SERVER_MODE, "false"));
		int nodes = properties.size() / 2;
		String readSelfIndexFromSys = System.getProperty(SERVER_NODE_SELF_INDEX, "0");
		int selfIndex = Integer.parseInt(
				properties.getProperty(SERVER_NODE_SELF_INDEX, readSelfIndexFromSys));
		for (int i = 0; i < nodes; i++) {
			String ip = properties.getProperty(SERVER_NODE_IP + i);
			String port = properties.getProperty(SERVER_NODE_PORT + i);
			ServerNode node = ServerNode.builder().nodeIp(ip).port(Integer.parseInt(port))
					.build();
			if (i == selfIndex) {
				nodeManager.setSelf(node);
				System.setProperty("server.port", node.getPort() + "");
			}
			// 单机模式并且为本节点
			if (isStandalone && i == selfIndex) {
				nodeManager.nodeJoin(node);
				break;
			}
			// 如果是集群模式，则加入节点管理
			else if (!isStandalone) {
				nodeManager.nodeJoin(node);
			}
		}
	}

	private final Comparator<ServerNode> nodeComparator = (o1, o2) -> {
		String k1 = o1.getKey();
		String k2 = o2.getKey();
		return k1.compareTo(k2);
	};

	private List<ServerNode> nodeCache = new ArrayList<>();

	private ServerNode self = null;

	private Set<NodeChangeListener> listeners = new LinkedHashSet<>();

	private Map<String, ServerNode> nodeMap = new ConcurrentHashMap<>(3);

	public static NodeManager getInstance() {
		return INSTANCE;
	}

	public synchronized void registerListener(NodeChangeListener listener) {
		listeners.add(listener);
	}

	public boolean isSelf(String key) {
		return Objects.equals(self.getKey(), key);
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
		if (nodeCache.isEmpty()) {
			nodeCache = new ArrayList<>(nodeMap.values());
			nodeCache.sort(nodeComparator);
		}
		return nodeCache;
	}

	private void notifyListener() {
		nodeCache.clear();
		Collection<ServerNode> nodes = serverNodes();
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
