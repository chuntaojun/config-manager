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

import com.lessspring.org.handler.ConfigHandler;
import com.lessspring.org.model.vo.DeleteConfigRequest;
import com.lessspring.org.model.vo.PublishConfigRequest;
import com.lessspring.org.model.vo.QueryConfigRequest;
import com.lessspring.org.service.config.ConfigOperationService;
import com.lessspring.org.utils.RenderUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Service(value = "configHandler")
public class ConfigHandlerImpl implements ConfigHandler {

    private final ConfigOperationService operationService;

    public ConfigHandlerImpl(ConfigOperationService operationService) {
        this.operationService = operationService;
    }

    @NotNull
    @Override
    public Mono<ServerResponse> publishConfig(ServerRequest request) {
        final String namespaceId = request.queryParam("namespaceId").orElse("default");
        return request.bodyToMono(PublishConfigRequest.class)
                .map(publishRequest -> operationService.publishConfig(namespaceId, publishRequest))
                .map(Mono::just)
                .flatMap(RenderUtils::render);
    }

    @NotNull
    @Override
    public Mono<ServerResponse> modifyConfig(ServerRequest request) {
        final String namespaceId = request.queryParam("namespaceId").orElse("default");
        return request.bodyToMono(PublishConfigRequest.class)
                .map(publishRequest -> operationService.modifyConfig(namespaceId, publishRequest))
                .map(Mono::just)
                .flatMap(RenderUtils::render);
    }

    @NotNull
    @Override
    public Mono<ServerResponse> queryConfig(ServerRequest request) {
        final String namespaceId = request.queryParam("namespaceId").orElse("default");
        return request.bodyToMono(QueryConfigRequest.class)
                .map(queryRequest -> operationService.queryConfig(namespaceId, queryRequest))
                .map(Mono::just)
                .flatMap(RenderUtils::render);
    }

    @NotNull
    @Override
    public Mono<ServerResponse> removeConfig(ServerRequest request) {
        final String namespaceId = request.queryParam("namespaceId").orElse("default");
        return request.bodyToMono(DeleteConfigRequest.class)
                .map(deleteRequest -> operationService.removeConfig(namespaceId, deleteRequest))
                .map(Mono::just)
                .flatMap(RenderUtils::render);
    }
}
