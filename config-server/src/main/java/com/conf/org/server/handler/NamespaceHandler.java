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
package com.conf.org.server.handler;

import reactor.core.publisher.Mono;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public interface NamespaceHandler {

	/**
	 * create namespace
	 *
	 * @param request {@link ServerRequest}
	 * @return {@link Mono<ServerResponse>}
	 */
	Mono<ServerResponse> createNamespace(ServerRequest request);

	/**
	 * delete namespace
	 *
	 * @param request {@link ServerRequest}
	 * @return {@link Mono<ServerResponse>}
	 */
	Mono<ServerResponse> deleteNamespace(ServerRequest request);

	/**
	 * query all namespace
	 *
	 * @param request {@link ServerRequest}
	 * @return {@link Mono<ServerResponse>}
	 */
	Mono<ServerResponse> queryAll(ServerRequest request);

	/**
	 * query this namespace owner
	 *
	 * @param request {@link ServerRequest}
	 * @return {@link Mono<ServerResponse>}
	 */
	Mono<ServerResponse> namespaceOwner(ServerRequest request);

}
