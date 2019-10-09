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

import com.lessspring.org.raft.vo.ServerNode;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class NodeManager {

    private ServerNode self = null;

    private Map<String, ServerNode> nodeMap = new ConcurrentHashMap<>(3);

    private static final NodeManager INSTANCE = new NodeManager();

    public static NodeManager getInstance() {
        return INSTANCE;
    }

    public ServerNode getSelf() {
        return self;
    }

    public void setSelf(ServerNode self) {
        this.self = self;
    }

    public void nodeJoin(ServerNode node) {
        nodeMap.putIfAbsent(node.getKey(), node);
    }

    public void nodeLeave(ServerNode node) {
        nodeMap.remove(node.getKey());
    }

    public Stream<Map.Entry<String, ServerNode>> stream() {
        return nodeMap.entrySet().stream();
    }

    public Collection<ServerNode> serverNodes() {
        return nodeMap.values();
    }

}
