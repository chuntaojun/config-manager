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
package com.lessspring.org.handler.impl;

import com.lessspring.org.configuration.security.NeedAuth;
import com.lessspring.org.handler.AuthHandler;
import com.lessspring.org.utils.PropertiesEnum;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * 用户授权接口
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Service(value = "authHandler")
public class AuthHandlerImpl implements AuthHandler {

	@Override
	@NeedAuth(role = PropertiesEnum.Role.ADMIN)
	public Mono<ServerResponse> createAuth(ServerRequest request) {
		return null;
	}

	@Override
	@NeedAuth(role = PropertiesEnum.Role.ADMIN)
	public Mono<ServerResponse> removeAuth(ServerRequest request) {
		return null;
	}
}
