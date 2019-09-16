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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.lessspring.org.event.EventType;
import com.lessspring.org.event.ServerNodeChangeEvent;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.pojo.vo.NodeChangeRequest;
import com.lessspring.org.raft.ServerChangeListener;
import com.lessspring.org.raft.vo.ServerNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component
public class ClusterManager {

    private final EventBus eventBus = new EventBus("ClusterManager-EventBus");

    private Map<String, ServerNode> nodeManager = new ConcurrentHashMap<>(3);

    @PostConstruct
    public void init() {
        eventBus.register(this);
        eventBus.register(new ServerChangeListener());
    }

    public ResponseData nodeAdd(NodeChangeRequest request) {
        nodeChange(request, EventType.PUBLISH);
        return ResponseData.success();
    }

    public ResponseData nodeRemove(NodeChangeRequest request) {
        nodeChange(request, EventType.DELETE);
        return ResponseData.success();
    }

    private void nodeChange(NodeChangeRequest request, EventType type) {
        ServerNodeChangeEvent event = ServerNodeChangeEvent.builder()
                .nodeIp(request.getNodeIp())
                .nodePort(request.getNodePort())
                .type(type)
                .build();
        publishEvent(event);
    }

    public ResponseData listNodes() {
        List<ServerNode> nodes = nodeManager.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
        return ResponseData.builder()
                .withCode(200)
                .withData(nodes)
                .build();
    }

    private void publishEvent(ServerNodeChangeEvent event) {
        eventBus.post(event);
    }

    @Subscribe
    public void onChange(ServerNodeChangeEvent event) {
        ServerNode node = ServerNode.builder()
                .nodeIp(event.getNodeIp())
                .port(event.getNodePort())
                .build();
        switch (event.getType()) {
            case PUBLISH:
                nodeManager.putIfAbsent(node.getKey(), node);
                break;
            case DELETE:
                nodeManager.remove(node.getKey());
                break;
            default:
                throw new IllegalArgumentException("Illegal cluster nodes change event type");
        }
    }

}
