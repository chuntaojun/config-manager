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

import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.server.configuration.security.NeedAuth;
import com.lessspring.org.server.handler.NamespaceHandler;
import com.lessspring.org.server.pojo.request.NamespaceRequest;
import com.lessspring.org.server.service.config.NamespaceService;
import com.lessspring.org.server.utils.PropertiesEnum;
import com.lessspring.org.server.utils.RenderUtils;
import org.apache.commons.lang.StringUtils;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * 命名空间管理接口
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Service(value = "namespaceHandler")
public class NamespaceHandlerImpl implements NamespaceHandler {

	private final NamespaceService namespaceService;

	public NamespaceHandlerImpl(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	@Override
	@NeedAuth(role = PropertiesEnum.Role.ADMIN)
	public Mono<ServerResponse> createNamespace(ServerRequest request) {
		return request.bodyToMono(NamespaceRequest.class)
				.map(namespaceService::createNamespace).flatMap(RenderUtils::render);
	}

	@Override
	@NeedAuth(role = PropertiesEnum.Role.ADMIN)
	public Mono<ServerResponse> deleteNamespace(ServerRequest request) {
		final String namespaceId = request.queryParam("namespaceId").orElse(null);
		if (StringUtils.isEmpty(namespaceId)) {
			return RenderUtils.render(ResponseData.fail("缺少「namespaceId」参数信息"));
		}
		return RenderUtils.render(namespaceService.removeNamespace(
				NamespaceRequest.builder().namespace(namespaceId).build()));
	}

	@Override
	public Mono<ServerResponse> queryAll(ServerRequest request) {
		return RenderUtils.render(namespaceService.queryAll());
	}

	@Override
	public Mono<ServerResponse> namespaceOwner(ServerRequest request) {
		final String namespaceId = request.queryParam("namespaceId").orElse("default");
		return RenderUtils.render(namespaceService.allOwnerByNamespace(namespaceId));
	}
}
