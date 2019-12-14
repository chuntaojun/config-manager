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
package com.lessspring.org.server.handler.impl;

import com.lessspring.org.server.configuration.security.NeedAuth;
import com.lessspring.org.server.handler.ClusterHandler;
import com.lessspring.org.server.pojo.request.NodeChangeRequest;
import com.lessspring.org.server.service.cluster.ClusterManager;
import com.lessspring.org.server.utils.PropertiesEnum;
import com.lessspring.org.server.utils.RenderUtils;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * 集群管理接口
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Service(value = "clusterHandler")
public class ClusterHandlerImpl implements ClusterHandler {

	private final ClusterManager clusterManager;

	public ClusterHandlerImpl(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

	@Override
	@NeedAuth(role = PropertiesEnum.Role.ADMIN)
	public Mono<ServerResponse> joinNode(ServerRequest request) {
		return request.bodyToMono(NodeChangeRequest.class).map(clusterManager::nodeAdd)
				.flatMap(RenderUtils::render);
	}

	@Override
	@NeedAuth(role = PropertiesEnum.Role.ADMIN)
	public Mono<ServerResponse> leaveNode(ServerRequest request) {
		return request.bodyToMono(NodeChangeRequest.class).map(clusterManager::nodeRemove)
				.flatMap(RenderUtils::render);
	}

	@Override
	@NeedAuth(role = PropertiesEnum.Role.ADMIN)
	public Mono<ServerResponse> serverNodes(ServerRequest request) {
		return RenderUtils.render(Mono.just(clusterManager.listNodes()));
	}
}
