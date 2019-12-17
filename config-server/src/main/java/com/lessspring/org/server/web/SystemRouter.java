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
import com.lessspring.org.server.handler.SystemHandler;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
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

		Tuple2<RequestPredicate, HandlerFunction> logAnalyze = Tuples
				.of(GET(StringConst.API_V1 + "sys/log/publish/analyze").and(
						accept(MediaType.APPLICATION_JSON_UTF8)), new HandlerFunction() {
							@Override
							public Mono handle(ServerRequest request) {
								return systemHandler.publishLog(request);
							}
						});

		Tuple2<RequestPredicate, HandlerFunction> heapDump = Tuples
				.of(GET(StringConst.API_V1 + "sys/jvm/heapDump").and(
						accept(MediaType.APPLICATION_JSON_UTF8)), new HandlerFunction() {
							@Override
							public Mono handle(ServerRequest request) {
								return systemHandler.jvmHeapDump(request);
							}
						});

		Tuple2<RequestPredicate, HandlerFunction> qps = Tuples
				.of(GET(StringConst.API_V1 + "sys/qps/setting").and(
						accept(MediaType.APPLICATION_JSON_UTF8)), new HandlerFunction() {
							@Override
							public Mono handle(ServerRequest request) {
								return systemHandler.queryQpsSetting(request);
							}
						});

		Tuple2<RequestPredicate, HandlerFunction> qpsSetting = Tuples
				.of(POST(StringConst.API_V1 + "sys/qps/setting/update").and(
						accept(MediaType.APPLICATION_JSON_UTF8)), new HandlerFunction() {
							@Override
							public Mono handle(ServerRequest request) {
								return systemHandler.publishQpsSetting(request);
							}
						});

		Tuple2<RequestPredicate, HandlerFunction> idManager = Tuples
				.of(GET(StringConst.API_V1 + "sys/idManager/info").and(
						accept(MediaType.APPLICATION_JSON_UTF8)), new HandlerFunction() {
							@Override
							public Mono handle(ServerRequest request) {
								return systemHandler.getAllTransactionIdInfo(request);
							}
						});

		Tuple2<RequestPredicate, HandlerFunction> logLevel = Tuples
				.of(POST(StringConst.API_V1 + "sys/logLevel/update").and(
						accept(MediaType.APPLICATION_JSON_UTF8)), new HandlerFunction() {
							@Override
							public Mono handle(ServerRequest request) {
								return systemHandler.changeLogLevel(request);
							}
						});

		Tuple2<RequestPredicate, HandlerFunction> forceDump = Tuples
				.of(POST(StringConst.API_V1 + "sys/config/forceDump").and(
						accept(MediaType.APPLICATION_JSON_UTF8)), new HandlerFunction() {
							@Override
							public Mono handle(ServerRequest request) {
								return systemHandler.forceDumpConfig(request);
							}
						});

		registerVisitor(logAnalyze, heapDump, qps, qpsSetting, idManager, logLevel,
				forceDump);

		return route(logAnalyze.getT1(), logAnalyze.getT2())
				.andRoute(heapDump.getT1(), heapDump.getT2())
				.andRoute(qps.getT1(), qps.getT2())
				.andRoute(qpsSetting.getT1(), qpsSetting.getT2())
				.andRoute(idManager.getT1(), idManager.getT2())
				.andRoute(logLevel.getT1(), logLevel.getT2())
				.andRoute(forceDump.getT1(), forceDump.getT2());
	}
}
