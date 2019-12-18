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
package com.lessspring.org.server.handler;

import reactor.core.publisher.Mono;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public interface ClusterHandler {

	/**
	 * Nodes to join
	 *
	 * @param request {@link ServerRequest}
	 * @return {@link Mono<ServerResponse>}
	 */
	Mono<ServerResponse> joinNode(ServerRequest request);

	/**
	 * Nodes to leave
	 *
	 * @param request {@link ServerRequest}
	 * @return {@link Mono<ServerResponse>}
	 */
	Mono<ServerResponse> leaveNode(ServerRequest request);

	/**
	 * For information on all the cluster nodes
	 *
	 * @param request {@link ServerRequest}
	 * @return {@link Mono<ServerResponse>}
	 */
	Mono<ServerResponse> serverNodes(ServerRequest request);

}
