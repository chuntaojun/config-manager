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
package com.lessspring.org.service.config.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.lessspring.org.db.dto.NamespaceDTO;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.pojo.request.NamespaceRequest;
import com.lessspring.org.pojo.vo.NamespaceVO;
import com.lessspring.org.raft.exception.TransactionException;
import com.lessspring.org.raft.pojo.Datum;
import com.lessspring.org.raft.pojo.Transaction;
import com.lessspring.org.repository.NamespaceMapper;
import com.lessspring.org.service.cluster.ClusterManager;
import com.lessspring.org.service.cluster.FailCallback;
import com.lessspring.org.service.config.NamespaceService;
import com.lessspring.org.service.distributed.BaseTransactionCommitCallback;
import com.lessspring.org.service.distributed.TransactionConsumer;
import com.lessspring.org.service.security.AuthorityProcessor;
import com.lessspring.org.utils.GsonUtils;
import com.lessspring.org.utils.IDUtils;
import com.lessspring.org.utils.PropertiesEnum;
import com.lessspring.org.utils.TransactionUtils;
import com.lessspring.org.utils.vo.NamespaceVOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Service(value = "namespaceService")
public class NamespaceServiceImpl implements NamespaceService {

	private final String createNamespace = "CREATE_NAMESPACE";
	private final String deleteNamespace = "DELETE_NAMESPACE";
	private final String createAuth4Namespace = "CREATA_AUTH_NAMESPACE";
	private final BaseTransactionCommitCallback commitCallback;
	private final ClusterManager clusterManager;
	private final AuthorityProcessor authorityProcessor;
	private LoadingCache<String, Optional<NamespaceDTO>> namespaceCache;
	@Resource
	private NamespaceMapper namespaceMapper;
	private FailCallback failCallback;

	public NamespaceServiceImpl(
			@Qualifier(value = "configTransactionCommitCallback") BaseTransactionCommitCallback commitCallback,
			ClusterManager clusterManager,
			AuthorityProcessor authorityProcessor) {
		this.commitCallback = commitCallback;
		this.clusterManager = clusterManager;
		this.authorityProcessor = authorityProcessor;
	}

	@PostConstruct
	public void init() {
		namespaceCache = CacheBuilder.newBuilder()
				.expireAfterWrite(Duration.ofMinutes(15)).maximumSize(65535)
				.build(new CacheLoader<>() {
					@Override
					public Optional<NamespaceDTO> load(String key) throws Exception {
						return Optional
								.ofNullable(namespaceMapper.findNamespaceDTOByName(key));
					}
				});
		commitCallback.registerConsumer(PropertiesEnum.Bz.NAMESPACE,
				createNamespaceConsumer(), createNamespace);
		commitCallback.registerConsumer(PropertiesEnum.Bz.NAMESPACE,
				removeNamespaceConsumer(), deleteNamespace);
		commitCallback.registerConsumer(PropertiesEnum.Bz.NAMESPACE,
				createAuth4NamespaceConsumer(), createAuth4Namespace);
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
		String key = TransactionUtils.buildTransactionKey(
				PropertiesEnum.InterestKey.CONFIG_DATA, request.getNamespace());
		Datum datum = new Datum(key, GsonUtils.toJsonBytes(request),
				NamespaceRequest.CLASS_NAME);
		datum.setOperation(createNamespace);
		return ResponseData.success();
	}

	@Override
	public ResponseData<?> removeNamespace(NamespaceRequest request) {
		String key = TransactionUtils.buildTransactionKey(
				PropertiesEnum.InterestKey.CONFIG_DATA, request.getNamespace());
		Datum datum = new Datum(key, GsonUtils.toJsonBytes(request),
				NamespaceRequest.CLASS_NAME);
		datum.setOperation(deleteNamespace);
		return commit(datum);
	}

	@Override
	public ResponseData<?> createNamespaceAuth(NamespaceRequest request) {
		String key = TransactionUtils.buildTransactionKey(
				PropertiesEnum.InterestKey.CONFIG_DATA, request.getNamespace());
		Datum datum = new Datum(key, GsonUtils.toJsonBytes(request),
				NamespaceRequest.CLASS_NAME);
		datum.setOperation(createAuth4Namespace);
		return commit(datum);
	}

	@Override
	public ResponseData<List<NamespaceVO>> queryAll() {
		Optional<List<NamespaceDTO>> dtos = Optional
				.ofNullable(namespaceMapper.queryAll());
		List<NamespaceVO> vos = new ArrayList<>();
		for (NamespaceDTO dto : dtos.orElse(Collections.emptyList())) {
			NamespaceVO vo = NamespaceVOUtils.change(dto);
			vos.add(vo);
		}
		return ResponseData.success(vos);
	}

	private TransactionConsumer<Transaction> createNamespaceConsumer() {
		return new TransactionConsumer<Transaction>() {
			@Override
			public void accept(Transaction transaction) throws Throwable {
				NamespaceRequest request = GsonUtils.toObj(transaction.getData(),
						NamespaceRequest.class);
				NamespaceDTO dto = NamespaceDTO.builder()
						.namespace(request.getNamespace())
						.namespaceId(IDUtils.generateUuid(request.getNamespace()))
						.build();
				namespaceMapper.saveNamespace(dto);
			}

			@Override
			public void onError(TransactionException te) {

			}
		};
	}

	private TransactionConsumer<Transaction> removeNamespaceConsumer() {
		return new TransactionConsumer<Transaction>() {
			@Override
			public void accept(Transaction transaction) throws Throwable {
				NamespaceRequest request = GsonUtils.toObj(transaction.getData(),
						NamespaceRequest.class);
				namespaceCache.invalidate(request.getNamespace());
				namespaceMapper.removeNamespace(request.getNamespace());
			}

			@Override
			public void onError(TransactionException te) {

			}
		};
	}

	private TransactionConsumer<Transaction> createAuth4NamespaceConsumer() {
		return new TransactionConsumer<Transaction>() {
			@Override
			public void accept(Transaction transaction) throws Throwable {
				NamespaceRequest request = GsonUtils.toObj(transaction.getData(),
						NamespaceRequest.class);
			}

			@Override
			public void onError(TransactionException te) {

			}
		};
	}

	private ResponseData<?> commit(Datum datum) {
		datum.setBz(PropertiesEnum.Bz.NAMESPACE.name());
		CompletableFuture<ResponseData<Boolean>> future = clusterManager.commit(datum,
				failCallback);
		try {
			return future.get(10_000L, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException | ExecutionException | TimeoutException e) {
			return ResponseData.fail(e);
		}
	}
}
