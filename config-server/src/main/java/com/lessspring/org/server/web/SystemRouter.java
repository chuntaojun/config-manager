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
import com.lessspring.org.server.configuration.ConfVisitor;
import com.lessspring.org.server.handler.SystemHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;
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
public class SystemRouter extends BaseRouter {

	private final SystemHandler systemHandler;

	public SystemRouter(SystemHandler systemHandler) {
		this.systemHandler = systemHandler;
	}

	@SuppressWarnings("all")
	@Bean(value = "systemRouterImpl")
	public RouterFunction<ServerResponse> systemRouter() {

		RequestPredicate logAnalyze = GET(StringConst.API_V1 + "sys/log/publish/analyze")
				.and(accept(MediaType.APPLICATION_JSON_UTF8));

		RequestPredicate heapDump = GET(StringConst.API_V1 + "sys/jvm/heapDump")
				.and(accept(MediaType.APPLICATION_JSON_UTF8));

		RequestPredicate qps = GET(StringConst.API_V1 + "sys/qps/setting")
				.and(accept(MediaType.APPLICATION_JSON_UTF8));

		RequestPredicate qpsSetting = POST(StringConst.API_V1 + "sys/qps/setting/update")
				.and(accept(MediaType.APPLICATION_JSON_UTF8));

		RequestPredicate idManager = GET(StringConst.API_V1 + "sys/idManager/info")
				.and(accept(MediaType.APPLICATION_JSON_UTF8));

		RequestPredicate logLevel = POST(StringConst.API_V1 + "sys/logLevel/update")
				.and(accept(MediaType.APPLICATION_JSON_UTF8));

		RequestPredicate forceDump = POST(StringConst.API_V1 + "sys/config/forceDump")
				.and(accept(MediaType.APPLICATION_JSON_UTF8));

		logAnalyze.accept(new ConfVisitor());
		heapDump.accept(new ConfVisitor());
		qps.accept(new ConfVisitor());
		qpsSetting.accept(new ConfVisitor());
		idManager.accept(new ConfVisitor());
		logLevel.accept(new ConfVisitor());
		forceDump.accept(new ConfVisitor());

		return route(logAnalyze,
				systemHandler::publishLog).andRoute(heapDump, systemHandler::jvmHeapDump)
						.andRoute(qps, systemHandler::queryQpsSetting)
						.andRoute(qpsSetting, systemHandler::publishQpsSetting)
						.andRoute(idManager, systemHandler::getAllTransactionIdInfo)
						.andRoute(logLevel, systemHandler::changeLogLevel)
						.andRoute(forceDump, systemHandler::forceDumpConfig);
	}
}
