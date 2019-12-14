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
package com.lessspring.org.server.web;

import com.lessspring.org.constant.StringConst;
import com.lessspring.org.server.handler.NamespaceHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Configuration
public class NamespaceRouter {

	private final NamespaceHandler namespaceHandler;

	public NamespaceRouter(NamespaceHandler namespaceHandler) {
		this.namespaceHandler = namespaceHandler;
	}

	@Bean(value = "namespaceRouterImpl")
	public RouterFunction<ServerResponse> namespaceRouter() {
		return route(
				PUT(StringConst.API_V1 + "namespace/create")
						.and(accept(MediaType.APPLICATION_JSON_UTF8)),
				namespaceHandler::createNamespace)
						.andRoute(
								DELETE(StringConst.API_V1 + "namespace/delete")
										.and(accept(MediaType.APPLICATION_JSON_UTF8)),
								namespaceHandler::deleteNamespace)
						.andRoute(
								GET(StringConst.API_V1 + "namespace/all")
										.and(accept(MediaType.APPLICATION_JSON_UTF8)),
								namespaceHandler::queryAll);
	}
}