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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import com.lessspring.org.LifeCycle;
import com.lessspring.org.raft.NodeChangeListener;
import com.lessspring.org.raft.NodeManager;
import com.lessspring.org.raft.vo.ServerNode;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
public class DistroRouter implements NodeChangeListener, LifeCycle {

	private static final DistroRouter ROUTER;

	static {
		ROUTER = new DistroRouter();
		ROUTER.init();
	}

	public static DistroRouter getInstance() {
		return ROUTER;
	}

	private DistroRouter() {
	};

	private final NodeManager nodeManager = NodeManager.getInstance();
	private AtomicReference<ServerNode[]> serverNodeAR = new AtomicReference<>();

	@Override
	public void init() {
		nodeManager.registerListener(this);
			ArrayList<ServerNode> serverNodes = new ArrayList<>(nodeManager.serverNodes());
			serverNodes.remove(nodeManager.getSelf());
		serverNodeAR.set(serverNodes.toArray(new ServerNode[0]));
	}

	@Override
	public void destroy() {
	}

	// Data fragmentation judgment, if the node is responsible for,
	// it returns null said without forwarding the request

	public ServerNode route(String key) {
		ServerNode[] nodes = serverNodeAR.get();
		int hash = distroHash(key);
		int index = hash % nodes.length;
		return Objects.equals(nodeManager.getSelf(), nodes[index]) ? nodeManager.getSelf()
				: nodes[index];
	}

	public boolean isPrincipal(String key) {
		return Objects.equals(route(key).getKey(), nodeManager.getSelf().getKey());
	}

	private int distroHash(String key) {
		return Math.abs(key.hashCode() % Integer.MAX_VALUE);
	}

	public String self() {
			return nodeManager.getSelf().getKey();
	}

	@Override
	public void onChange(Collection<ServerNode> newServerNodes) {
		serverNodeAR.set(newServerNodes.toArray(new ServerNode[0]));
	}
}
