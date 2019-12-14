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
import com.lessspring.org.server.handler.UserHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Configuration
public class UserRouter {

	private final UserHandler userHandler;

	public UserRouter(UserHandler userHandler) {
		this.userHandler = userHandler;
	}

	@Bean(value = "userRouterImpl")
	public RouterFunction<ServerResponse> userRouterImpl() {
		return route(POST(StringConst.API_V1 + "login")
				.and(accept(MediaType.APPLICATION_JSON_UTF8)), userHandler::login)
						.andRoute(
								PUT(StringConst.API_V1 + "createUser")
										.and(accept(MediaType.APPLICATION_JSON_UTF8)),
								userHandler::createUser)
						.andRoute(
								DELETE(StringConst.API_V1 + "removeUser")
										.and(accept(MediaType.APPLICATION_JSON_UTF8)),
								userHandler::removeUser)
						.andRoute(
								POST(StringConst.API_V1 + "modifyUser")
										.and(accept(MediaType.APPLICATION_JSON_UTF8)),
								userHandler::modifyUser)
						.andRoute(
								GET(StringConst.API_V1 + "allUser")
										.and(accept(MediaType.APPLICATION_JSON_UTF8)),
								userHandler::queryAll);
	}

}
