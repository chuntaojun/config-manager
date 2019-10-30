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
package com.lessspring.org.service.security.impl;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.pojo.Privilege;
import com.lessspring.org.repository.NamespacePermissionsMapper;
import com.lessspring.org.service.security.AuthorityProcessor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component(value = "authorityProcessor")
public class AuthorityProcessorImpl implements AuthorityProcessor {

	@Resource
	private NamespacePermissionsMapper permissionsMapper;

	@Override
	public boolean hasAuth(Privilege privilege, String namespaceId) {
		Set<String> namespaceIds = new HashSet<>(privilege.getOwnerNamespaces());
		if (namespaceIds.contains(namespaceId)) {
			return true;
		}
		namespaceIds = new HashSet<>(
				permissionsMapper.findNamespaceIdByUserId(privilege.getUserId()));
		return namespaceIds.contains(namespaceId);
	}

	@Override
	public ResponseData<?> createAuth(String namespaceId) {
		return null;
	}
}
