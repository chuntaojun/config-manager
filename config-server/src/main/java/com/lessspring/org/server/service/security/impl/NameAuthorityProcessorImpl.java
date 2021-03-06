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
package com.lessspring.org.server.service.security.impl;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.server.pojo.Privilege;
import com.lessspring.org.server.repository.NamespacePermissionsMapper;
import com.lessspring.org.server.repository.UserMapper;
import com.lessspring.org.server.service.security.AuthorityProcessor;
import com.lessspring.org.server.utils.PropertiesEnum;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Primary
@Component(value = "namespaceAuthorityProcessor")
public class NameAuthorityProcessorImpl implements AuthorityProcessor {

	@Resource
	private NamespacePermissionsMapper permissionsMapper;

	@Resource
	private UserMapper userMapper;

	@Override
	public boolean hasAuth(Privilege privilege, PropertiesEnum.Role role) {
		final String namespaceId = privilege.getAttachment("namespaceId");
		Set<String> namespaceIds = new HashSet<>(privilege.getOwnerNamespaces());
		if (namespaceIds.contains(namespaceId)) {
			return true;
		}
		// verify role
		boolean hasRole = role.equals(privilege.getRole());
		boolean hasResource = false;
		// If not an administrator, you need to validate namespace rights
		if (hasRole && !PropertiesEnum.Role.ADMIN.equals(privilege.getRole())) {
			// verify resource
			namespaceIds = new HashSet<>(
					permissionsMapper.findNamespaceIdByUserId(privilege.getUserId()));
			hasResource = namespaceIds.contains(namespaceId);
		}
		return hasRole && hasResource;
	}

	@Override
	public ResponseData<?> createAuth(String namespaceId, PropertiesEnum.Role role) {
		return null;
	}
}
