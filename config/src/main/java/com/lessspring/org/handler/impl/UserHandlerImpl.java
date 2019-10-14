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

import com.lessspring.org.handler.UserHandler;
import com.lessspring.org.model.vo.LoginRequest;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.service.security.SecurityService;
import com.lessspring.org.utils.RenderUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Configuration
public class UserHandlerImpl implements UserHandler {

    private final SecurityService securityService;

    public UserHandlerImpl(SecurityService securityService) {
        this.securityService = securityService;
    }

    @NotNull
    @Override
    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginRequest.class)
                .map(securityService::apply4Authorization)
                .map(ResponseData::success)
                .map(Mono::just)
                .flatMap(RenderUtils::render);
    }

    @NotNull
    @Override
    public Mono<ServerResponse> createUser(ServerRequest request) {
        return null;
    }

    @NotNull
    @Override
    public Mono<ServerResponse> removeUser(ServerRequest request) {
        return null;
    }

    @NotNull
    @Override
    public Mono<ServerResponse> modifyUser(ServerRequest request) {
        return null;
    }
}
