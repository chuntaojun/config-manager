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
package com.conf.org.server.service.config.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.conf.org.IDUtils;
import com.conf.org.db.dto.NamespaceDTO;
import com.conf.org.model.vo.ResponseData;
import com.conf.org.server.repository.NamespaceMapper;
import com.conf.org.server.repository.NamespacePermissionsMapper;
import com.conf.org.server.service.cluster.ClusterManager;
import com.conf.org.server.service.cluster.FailCallback;
import com.conf.org.server.service.distributed.BaseTransactionCommitCallback;
import com.conf.org.server.service.security.AuthorityProcessor;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.conf.org.server.pojo.request.NamespaceRequest;
import com.conf.org.server.pojo.vo.NamespaceVO;
import com.conf.org.server.service.config.NamespaceService;
import com.conf.org.server.utils.VOUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Service(value = "namespaceService")
public class NamespaceServiceImpl implements NamespaceService {

	private final BaseTransactionCommitCallback commitCallback;
	private final ClusterManager clusterManager;
	private final AuthorityProcessor authorityProcessor;
	private LoadingCache<String, Optional<NamespaceDTO>> namespaceCache;
	@Resource
	private NamespaceMapper namespaceMapper;
	@Resource
	private NamespacePermissionsMapper permissionsMapper;
	private FailCallback failCallback;

	public NamespaceServiceImpl(BaseTransactionCommitCallback commitCallback,
			ClusterManager clusterManager, AuthorityProcessor authorityProcessor) {
		this.commitCallback = commitCallback;
		this.clusterManager = clusterManager;
		this.authorityProcessor = authorityProcessor;
	}

	@PostConstruct
	public void init() {
		namespaceCache = CacheBuilder.newBuilder()
				.expireAfterWrite(Duration.ofMinutes(15)).maximumSize(65535)
				.build(new CacheLoader<String, Optional<NamespaceDTO>>() {
					@Override
					public Optional<NamespaceDTO> load(String key) throws Exception {
						return Optional
								.ofNullable(namespaceMapper.findNamespaceDTOByName(key));
					}
				});
		failCallback = throwable -> null;
	}

	@Override
	public ResponseData<String> findOneNamespaceByName(String name) {
		final String[] namespaceName = new String[] { null };
		Optional<NamespaceDTO> optional = namespaceCache.getUnchecked(name);
		optional.ifPresent(dto -> namespaceName[0] = dto.getNamespace());
		return ResponseData.success(namespaceName[0]);
	}

	@Override
	public ResponseData<?> createNamespace(NamespaceRequest request) {
		if (StringUtils.isEmpty(request.getNamespaceId())) {
			request.setNamespaceId(IDUtils.generateUuid(request.getNamespace()));
		}
		else {
			Integer count = namespaceMapper.countById(request.getNamespaceId());
			if (count > 0) {
				return ResponseData.fail("this namespace-id already exist");
			}
		}
		NamespaceDTO dto = NamespaceDTO.builder().namespace(request.getNamespace())
				.namespaceId(request.getNamespaceId()).build();
		namespaceMapper.saveNamespace(dto);
		return ResponseData.success();
	}

	@Override
	public ResponseData<?> removeNamespace(NamespaceRequest request) {
		namespaceCache.invalidate(request.getNamespace());
		namespaceMapper.removeNamespace(request.getNamespace());
		return ResponseData.success();
	}

	@Override
	public ResponseData<List<NamespaceVO>> queryAll() {
		Optional<List<NamespaceDTO>> dtos = Optional
				.ofNullable(namespaceMapper.queryAll());
		List<NamespaceVO> vos = new ArrayList<>();
		for (NamespaceDTO dto : dtos.orElse(Collections.emptyList())) {
			NamespaceVO vo = VOUtils.convertNamespaceVO(dto);
			vos.add(vo);
		}
		return ResponseData.success(vos);
	}

	@Override
	public ResponseData<List<String>> allOwnerByNamespace(String namespaceId) {
		List<String> result = permissionsMapper.findUsersByNamespaceId(namespaceId);
		return ResponseData.success(
				CollectionUtils.isEmpty(result) ? Collections.emptyList() : result);
	}

}
