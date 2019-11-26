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
package com.lessspring.org.web;

import com.lessspring.org.constant.StringConst;
import com.lessspring.org.handler.SystemHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Configuration
public class SystemRouter {

	private final SystemHandler systemHandler;

	public SystemRouter(SystemHandler systemHandler) {
		this.systemHandler = systemHandler;
	}

	@Bean(value = "systemRouterImpl")
	public RouterFunction<ServerResponse> systemRouter() {
		return route(
				GET(StringConst.API_V1 + "sys/log/publish/analyze")
						.and(accept(MediaType.APPLICATION_JSON_UTF8)),
				systemHandler::publishLog)
						.andRoute(
								GET(StringConst.API_V1 + "sys/jvm/heapDump")
										.and(accept(MediaType.APPLICATION_JSON_UTF8)),
								systemHandler::jvmHeapDump)
						.andRoute(
								GET(StringConst.API_V1 + "sys/qps/setting")
										.and(accept(MediaType.APPLICATION_JSON_UTF8)),
								systemHandler::queryQpsSetting)
						.andRoute(
								POST(StringConst.API_V1 + "sys/qps/setting/update")
										.and(accept(MediaType.APPLICATION_JSON_UTF8)),
								systemHandler::publishQpsSetting);
	}
}
