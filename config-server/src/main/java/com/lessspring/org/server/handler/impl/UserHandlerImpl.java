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
package com.lessspring.org.server.handler.impl;

import com.lessspring.org.model.vo.LoginRequest;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.model.vo.UserQueryPage;
import com.lessspring.org.server.configuration.security.NeedAuth;
import com.lessspring.org.server.handler.UserHandler;
import com.lessspring.org.server.pojo.request.UserRequest;
import com.lessspring.org.server.service.security.SecurityService;
import com.lessspring.org.server.service.user.UserService;
import com.lessspring.org.server.utils.PropertiesEnum;
import com.lessspring.org.server.utils.RenderUtils;
import reactor.core.publisher.Mono;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Configuration
public class UserHandlerImpl implements UserHandler {

	private final SecurityService securityService;
	private final UserService userService;

	public UserHandlerImpl(SecurityService securityService, UserService userService) {
		this.securityService = securityService;
		this.userService = userService;
	}

	@Override
	public Mono<ServerResponse> login(ServerRequest request) {
		return request.bodyToMono(LoginRequest.class)
				.map(securityService::apply4Authorization).map(ResponseData::success)
				.map(Mono::just).flatMap(RenderUtils::render);
	}

	@Override
	@NeedAuth(role = PropertiesEnum.Role.ADMIN)
	public Mono<ServerResponse> createUser(ServerRequest request) {
		return request.bodyToMono(UserRequest.class).map(userService::createUser)
				.map(Mono::just).flatMap(RenderUtils::render);
	}

	@Override
	@NeedAuth(role = PropertiesEnum.Role.ADMIN)
	public Mono<ServerResponse> removeUser(ServerRequest request) {
		return request.bodyToMono(UserRequest.class).map(userService::removeUser)
				.map(Mono::just).flatMap(RenderUtils::render);
	}

	@Override
	public Mono<ServerResponse> modifyUser(ServerRequest request) {
		return request.bodyToMono(UserRequest.class).map(userService::modifyUser)
				.map(Mono::just).flatMap(RenderUtils::render);
	}

	@Override
	@NeedAuth(role = PropertiesEnum.Role.ADMIN)
	public Mono<ServerResponse> queryAll(ServerRequest request) {
		final long limit = Long.parseLong(request.queryParam("limit").orElse("10"));
		final long offset = Long.parseLong(request.queryParam("offset").orElse("0"));
		final UserQueryPage queryPage = UserQueryPage.builder()
				.username(request.queryParam("limit").orElse(null))
				.limit(limit)
				.offset(offset)
				.build();
		return RenderUtils.render(Mono.just(userService.queryAll(queryPage)));
	}
}
