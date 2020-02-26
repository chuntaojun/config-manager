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
import com.conf.org.server.handler.AuthHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Configuration
@SuppressWarnings("all")
public class AuthRouter extends BaseRouter {

	private final AuthHandler authHandler;

	public AuthRouter(AuthHandler authHandler) {
		this.authHandler = authHandler;
	}

	@Bean(value = "authRouterImpl")
	public RouterFunction<ServerResponse> authRouter() {

		Tuple2<RequestPredicate, HandlerFunction> createAuth = Tuples.of(
				POST(StringConst.API_V1 + "auth/create").and(accept(MediaType.APPLICATION_JSON_UTF8)), authHandler::createAuth
		);

		Tuple2<RequestPredicate, HandlerFunction> removeAuth = Tuples.of(
				POST(StringConst.API_V1 + "auth/remove").and(accept(MediaType.APPLICATION_JSON_UTF8)), authHandler::removeAuth
		);

		registerVisitor(createAuth, removeAuth);

		return route(createAuth.getT1(), createAuth.getT2())
				.andRoute(removeAuth.getT1(), removeAuth.getT2());
	}

}
