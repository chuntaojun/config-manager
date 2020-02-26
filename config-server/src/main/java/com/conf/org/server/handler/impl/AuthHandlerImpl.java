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
package com.conf.org.server.handler.impl;

import java.util.Objects;

import javax.annotation.Resource;

import com.conf.org.constant.Code;
import com.conf.org.db.dto.AuthorityDTO;
import com.conf.org.db.dto.UserDTO;
import com.conf.org.model.vo.ResponseData;
import com.conf.org.server.repository.NamespaceMapper;
import com.conf.org.server.repository.NamespacePermissionsMapper;
import com.conf.org.server.repository.UserMapper;
import com.conf.org.server.handler.AuthHandler;
import com.conf.org.server.pojo.request.AuthRequest;
import com.conf.org.server.utils.RenderUtils;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * 用户授权接口
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@SuppressWarnings("all")
@Service(value = "authHandler")
public class AuthHandlerImpl implements AuthHandler {

	@Resource
	private UserMapper userMapper;

	@Resource
	private NamespaceMapper namespaceMapper;

	@Resource
	private NamespacePermissionsMapper permissionsMapper;

	@Override
	public Mono<ServerResponse> createAuth(ServerRequest request) {
		Mono<AuthRequest> authRequestMono = request.bodyToMono(AuthRequest.class);
		Mono<ResponseData<String>> responseDataMono = authRequestMono.map(authRequest -> {
			return operation(authRequest, true);
		});
		return RenderUtils.render(responseDataMono);
	}

	@Override
	public Mono<ServerResponse> removeAuth(ServerRequest request) {
		Mono<AuthRequest> authRequestMono = request.bodyToMono(AuthRequest.class);
		Mono<ResponseData<String>> responseDataMono = authRequestMono.map(authRequest -> {
			return operation(authRequest, false);
		});
		return RenderUtils.render(responseDataMono);
	}

	private ResponseData<String> operation(AuthRequest authRequest, boolean isCreate) {
		final String username = authRequest.getUserName();
		final String namespaceId = authRequest.getNamespaceId();
		UserDTO userDTO = userMapper.findUserByName(username);
		if (Objects.isNull(userDTO)) {
			return ResponseData.fail(Code.USER_NOT_FOUNT);
		}
		Integer count = namespaceMapper.countById(namespaceId);
		if (Objects.isNull(count) || count < 1) {
			return ResponseData.fail("this namespace did't find");
		}
		boolean result = false;
		Integer affect = null;
		if (isCreate) {
			affect = permissionsMapper.savePermission(AuthorityDTO.builder()
					.namespaceId(namespaceId).userId(userDTO.getId()).build());
		}
		else {
			affect = permissionsMapper.removePermissionByUserIdAndNamespaceId(namespaceId,
					userDTO.getId());
		}
		result = Objects.nonNull(affect) && affect == 1;

		return ResponseData.<String> builder().withData(result ? "success" : "failed")
				.build();
	}
}
