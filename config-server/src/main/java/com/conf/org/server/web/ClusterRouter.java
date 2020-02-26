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
package com.conf.org.server.web;

import com.conf.org.api.ApiConstant;
import com.conf.org.server.handler.ClusterHandler;
import reactor.util.function.Tuples;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Configuration
@SuppressWarnings("all")
public class ClusterRouter extends BaseRouter {

	private final ClusterHandler clusterHandler;

	public ClusterRouter(ClusterHandler clusterHandler) {
		this.clusterHandler = clusterHandler;
	}

	@Bean(value = "clusterRouterImpl")
	public RouterFunction<ServerResponse> clusterRouter() {

		RequestPredicate nodeJoin = PUT(ApiConstant.CLUSTER_NODE_JOIN)
				.and(accept(MediaType.APPLICATION_JSON_UTF8));

		RequestPredicate allNodes = GET(ApiConstant.REFRESH_CLUSTER_NODE_INFO)
				.and(accept(MediaType.APPLICATION_JSON_UTF8));

		RequestPredicate removeNode = DELETE(ApiConstant.CLUSTER_NODE_LEAVE)
				.and(accept(MediaType.APPLICATION_JSON_UTF8));

		HandlerFunction<ServerResponse> nodeJoinHF = clusterHandler::joinNode;
		HandlerFunction<ServerResponse> serverNodesHF = clusterHandler::serverNodes;
		HandlerFunction<ServerResponse> removeNodeHF = clusterHandler::leaveNode;

		registerVisitor(Tuples.of(nodeJoin, nodeJoinHF),
				Tuples.of(allNodes, serverNodesHF), Tuples.of(removeNode, removeNodeHF));

		RouterFunction<ServerResponse> function = route(nodeJoin, nodeJoinHF)
				.andRoute(allNodes, serverNodesHF).andRoute(removeNode, removeNodeHF);
		return function;
	}

}
