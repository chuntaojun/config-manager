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

import com.conf.org.constant.StringConst;
import com.conf.org.server.handler.UserHandler;
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
public class UserRouter extends BaseRouter {

	private final UserHandler userHandler;

	public UserRouter(UserHandler userHandler) {
		this.userHandler = userHandler;
	}

	@Bean(value = "userRouterImpl")
	public RouterFunction<ServerResponse> userRouterImpl() {

		Tuple2<RequestPredicate, HandlerFunction> login = Tuples
				.of(POST(StringConst.API_V1 + "login").and(
						accept(MediaType.APPLICATION_JSON_UTF8)), new HandlerFunction() {
							@Override
							public Mono handle(ServerRequest request) {
								return userHandler.login(request);
							}
						});

		Tuple2<RequestPredicate, HandlerFunction> createUser = Tuples
				.of(PUT(StringConst.API_V1 + "user/create").and(
						accept(MediaType.APPLICATION_JSON_UTF8)), new HandlerFunction() {
							@Override
							public Mono handle(ServerRequest request) {
								return userHandler.createUser(request);
							}
						});

		Tuple2<RequestPredicate, HandlerFunction> deleteUser = Tuples
				.of(DELETE(StringConst.API_V1 + "user/remove").and(
						accept(MediaType.APPLICATION_JSON_UTF8)), new HandlerFunction() {
							@Override
							public Mono handle(ServerRequest request) {
								return userHandler.removeUser(request);
							}
						});

		Tuple2<RequestPredicate, HandlerFunction> modifyUser = Tuples
				.of(POST(StringConst.API_V1 + "user/modify").and(
						accept(MediaType.APPLICATION_JSON_UTF8)), new HandlerFunction() {
							@Override
							public Mono handle(ServerRequest request) {
								return userHandler.modifyUser(request);
							}
						});

		Tuple2<RequestPredicate, HandlerFunction> allUser = Tuples
				.of(GET(StringConst.API_V1 + "user/all").and(
						accept(MediaType.APPLICATION_JSON_UTF8)), new HandlerFunction() {
							@Override
							public Mono handle(ServerRequest request) {
								return userHandler.queryAll(request);
							}
						});

		registerVisitor(login, createUser, deleteUser, modifyUser, allUser);

		return route(login.getT1(), login.getT2())
				.andRoute(createUser.getT1(), createUser.getT2())
				.andRoute(deleteUser.getT1(), deleteUser.getT2())
				.andRoute(modifyUser.getT1(), modifyUser.getT2())
				.andRoute(allUser.getT1(), allUser.getT2());
	}

}
