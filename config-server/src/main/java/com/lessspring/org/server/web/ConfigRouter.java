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
import com.lessspring.org.server.handler.ConfigHandler;
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

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Configuration
@SuppressWarnings("all")
public class ConfigRouter extends BaseRouter {

	private final ConfigHandler configHandler;

	public ConfigRouter(ConfigHandler configHandler) {
		this.configHandler = configHandler;
	}

	@Bean(value = "configRouterImpl")
	public RouterFunction<ServerResponse> configRouter() {

		Tuple2<RequestPredicate, HandlerFunction> creatcC = Tuples.of(
				PUT(StringConst.API_V1 + "publish/config")
						.and(accept(MediaType.APPLICATION_JSON_UTF8)),
				new HandlerFunction<ServerResponse>() {
					@Override
					public Mono<ServerResponse> handle(ServerRequest request) {
						return configHandler.publishConfig(request);
					}
				});

		Tuple2<RequestPredicate, HandlerFunction> updateC = Tuples.of(
				POST(StringConst.API_V1 + "update/config")
						.and(accept(MediaType.APPLICATION_JSON_UTF8)),
				new HandlerFunction<ServerResponse>() {
					@Override
					public Mono<ServerResponse> handle(ServerRequest request) {
						return configHandler.modifyConfig(request);
					}
				});

		Tuple2<RequestPredicate, HandlerFunction> deleteC = Tuples.of(
				DELETE(StringConst.API_V1 + "delete/config")
						.and(accept(MediaType.APPLICATION_JSON_UTF8)),
				new HandlerFunction<ServerResponse>() {
					@Override
					public Mono<ServerResponse> handle(ServerRequest request) {
						return configHandler.removeConfig(request);
					}
				});

		Tuple2<RequestPredicate, HandlerFunction> queryC = Tuples.of(
				GET(StringConst.API_V1 + "query/config")
						.and(accept(MediaType.APPLICATION_JSON_UTF8)),
				new HandlerFunction<ServerResponse>() {
					@Override
					public Mono<ServerResponse> handle(ServerRequest request) {
						return configHandler.queryConfig(request);
					}
				});

		Tuple2<RequestPredicate, HandlerFunction> listC = Tuples.of(
				GET(StringConst.API_V1 + "config/list")
						.and(accept(MediaType.APPLICATION_JSON_UTF8)),
				new HandlerFunction<ServerResponse>() {
					@Override
					public Mono<ServerResponse> handle(ServerRequest request) {
						return configHandler.configList(request);
					}
				});

		Tuple2<RequestPredicate, HandlerFunction> detailC = Tuples.of(
				GET(StringConst.API_V1 + "config/detail")
						.and(accept(MediaType.APPLICATION_JSON_UTF8)),
				new HandlerFunction<ServerResponse>() {
					@Override
					public Mono<ServerResponse> handle(ServerRequest request) {
						return configHandler.configDetail(request);
					}
				});

		registerVisitor(creatcC, updateC, deleteC, queryC, listC, detailC);

		RouterFunction<ServerResponse> function = route(creatcC.getT1(), creatcC.getT2())
				.andRoute(updateC.getT1(), creatcC.getT2())
				.andRoute(deleteC.getT1(), deleteC.getT2())
				.andRoute(queryC.getT1(), queryC.getT2())
				.andRoute(listC.getT1(), listC.getT2())
				.andRoute(detailC.getT1(), detailC.getT2());
		return function;
	}
}
