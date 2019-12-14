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

import com.lessspring.org.HashUtils;
import com.lessspring.org.LifeCycle;
import com.lessspring.org.raft.NodeChangeListener;
import com.lessspring.org.raft.NodeManager;
import com.lessspring.org.raft.vo.ServerNode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

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

	private final NodeManager nodeManager = NodeManager.getInstance();
	private AtomicReference<ServerNode[]> serverNodeAR = new AtomicReference<>();;

	private DistroRouter() {
	}

	public static DistroRouter getInstance() {
		return ROUTER;
	}

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

	@Override
	public boolean isInited() {
		return false;
	}

	@Override
	public boolean isDestroyed() {
		return false;
	}

	// Data fragmentation judgment, if the node is responsible for,
	// it returns null said without forwarding the request

	public ServerNode route(String key) {
		ServerNode[] nodes = serverNodeAR.get();
		int index = HashUtils.distroHash(key, nodes.length);
		ServerNode targetNode = Objects.equals(nodeManager.getSelf(), nodes[index])
				? nodeManager.getSelf()
				: nodes[index];
		log.warn("[DistroRouter] has router to ServerNode : {}", targetNode);
		return targetNode;
	}

	public boolean isPrincipal(String key) {
		return Objects.equals(route(key).getKey(), nodeManager.getSelf().getKey());
	}

	public String self() {
		return nodeManager.getSelf().getKey();
	}

	@Override
	public void onChange(Collection<ServerNode> newServerNodes) {
		serverNodeAR.set(newServerNodes.toArray(new ServerNode[0]));
	}
}
