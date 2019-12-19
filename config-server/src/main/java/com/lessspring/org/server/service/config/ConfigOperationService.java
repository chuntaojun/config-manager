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
package com.lessspring.org.server.service.config;

import com.lessspring.org.db.dto.ConfigBetaInfoDTO;
import com.lessspring.org.db.dto.ConfigInfoDTO;
import com.lessspring.org.event.EventType;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.model.vo.BaseConfigRequest;
import com.lessspring.org.model.vo.ConfigQueryPage;
import com.lessspring.org.model.vo.DeleteConfigRequest;
import com.lessspring.org.model.vo.PublishConfigRequest;
import com.lessspring.org.model.vo.QueryConfigRequest;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.observer.Occurrence;
import com.lessspring.org.observer.Publisher;
import com.lessspring.org.observer.Watcher;
import com.lessspring.org.raft.exception.TransactionException;
import com.lessspring.org.raft.pojo.Datum;
import com.lessspring.org.raft.pojo.Transaction;
import com.lessspring.org.server.exception.NotThisResourceException;
import com.lessspring.org.server.pojo.event.config.ConfigChangeEvent;
import com.lessspring.org.server.pojo.event.config.ConfigChangeEventHandler;
import com.lessspring.org.server.pojo.event.config.NotifyEvent;
import com.lessspring.org.server.pojo.event.config.NotifyEventHandler;
import com.lessspring.org.server.pojo.request.DeleteConfigHistory;
import com.lessspring.org.server.pojo.request.DeleteConfigRequest4;
import com.lessspring.org.server.pojo.request.NamespaceRequest;
import com.lessspring.org.server.pojo.request.PublishConfigHistory;
import com.lessspring.org.server.pojo.request.PublishConfigRequest4;
import com.lessspring.org.server.pojo.vo.ConfigDetailVO;
import com.lessspring.org.server.pojo.vo.ConfigListVO;
import com.lessspring.org.server.service.cluster.ClusterManager;
import com.lessspring.org.server.service.cluster.FailCallback;
import com.lessspring.org.server.service.distributed.BaseTransactionCommitCallback;
import com.lessspring.org.server.service.distributed.TransactionConsumer;
import com.lessspring.org.server.service.publish.AbstractNotifyServiceImpl;
import com.lessspring.org.server.utils.BzConstants;
import com.lessspring.org.server.utils.DisruptorFactory;
import com.lessspring.org.server.utils.GsonUtils;
import com.lessspring.org.server.utils.PropertiesEnum;
import com.lessspring.org.server.utils.TransactionUtils;
import com.lessspring.org.server.utils.VOUtils;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Service
public class ConfigOperationService
		implements OperationService, WorkHandler<ConfigChangeEventHandler>, Watcher {

	private final String createConfig = "CREATE_CONFIG";
	private final String modifyConfig = "MODIFY_CONFIG";
	private final String deleteConfig = "DELETE_CONFIG";

	private final String createConfigHistory = "CREATE_CONFIG_HISTORY";
	private final String deleteConfigHistory = "DELETE_CONFIG_HISTORY";

	private final NamespaceService namespaceService;
	private final Disruptor<ConfigChangeEventHandler> changeEventDisruptor;
	private final Disruptor<NotifyEventHandler> notifyEventDisruptor;
	private final PersistentHandler persistentHandler;
	private final BaseTransactionCommitCallback commitCallback;
	private final ClusterManager clusterManager;
	private FailCallback failCallback;
	private ConfigCacheItemManager configCacheItemManager;

	public ConfigOperationService(PersistentHandler persistentHandler,
			NamespaceService namespaceService,
			BaseTransactionCommitCallback commitCallback, ClusterManager clusterManager,
			List<AbstractNotifyServiceImpl> notifyServices,
			ConfigCacheItemManager configCacheItemManager) {
		this.persistentHandler = persistentHandler;
		this.namespaceService = namespaceService;
		this.clusterManager = clusterManager;
		this.commitCallback = commitCallback;
		this.configCacheItemManager = configCacheItemManager;
		changeEventDisruptor = DisruptorFactory.build(ConfigChangeEventHandler::new,
				ConfigChangeEvent.class);
		changeEventDisruptor.handleEventsWithWorkerPool(this);
		changeEventDisruptor.start();
		notifyEventDisruptor = DisruptorFactory.build(NotifyEventHandler::new,
				NotifyEvent.class);
		notifyEventDisruptor.handleEventsWithWorkerPool(
				notifyServices.toArray(new AbstractNotifyServiceImpl[0]));
		notifyEventDisruptor.start();

		registerToPublisher();

	}

	@SuppressWarnings("unchecked")
	private void registerToPublisher() {
		persistentHandler.getPublisher().registerWatcher(this);
	}

	@PostConstruct
	public void init() {
		commitCallback.registerConsumer(PropertiesEnum.Bz.CONFIG, publishConsumer(),
				createConfig);
		commitCallback.registerConsumer(PropertiesEnum.Bz.CONFIG, modifyConsumer(),
				modifyConfig);
		commitCallback.registerConsumer(PropertiesEnum.Bz.CONFIG, deleteConsumer(),
				deleteConfig);
		commitCallback.registerConsumer(PropertiesEnum.Bz.CONFIG,
				saveConfigHistoryConsumer(), createConfigHistory);
		commitCallback.registerConsumer(PropertiesEnum.Bz.CONFIG,
				deleteConfigHistoryConsumer(), deleteConfigHistory);
		failCallback = throwable -> null;
	}

	@PreDestroy
	public void shutdown() {
		changeEventDisruptor.shutdown();
		clusterManager.destroy();
	}

	@Override
	public ResponseData<?> queryConfig(String namespaceId, QueryConfigRequest request) {
		ConfigInfo info = persistentHandler.readConfigContent(namespaceId, request);
		if (Objects.isNull(info)) {
			throw new NotThisResourceException("Config-Info data not found");
		}
		return ResponseData.success(info);
	}

	@Override
	public ResponseData<?> publishConfig(String namespaceId,
			PublishConfigRequest request) {
		PublishConfigRequest4 request4 = PublishConfigRequest4.copy(namespaceId, request);
		String key;
		if (request.isBeta()) {
			key = TransactionUtils.buildTransactionKey(
					PropertiesEnum.InterestKey.CONFIG_DATA, BzConstants.CONFIG_INFO_BETA,
					namespaceId, request.getGroupId(), request.getDataId());
		}
		else {
			key = TransactionUtils.buildTransactionKey(
					PropertiesEnum.InterestKey.CONFIG_DATA, BzConstants.CONFIG_INFO,
					namespaceId, request.getGroupId(), request.getDataId());
		}
		Datum datum = new Datum(key, GsonUtils.toJsonBytes(request4),
				PublishConfigRequest4.CLASS_NAME);
		datum.setOperation(createConfig);
		return commit(datum);
	}

	@Override
	public ResponseData<?> modifyConfig(String namespaceId,
			PublishConfigRequest request) {
		PublishConfigRequest4 request4 = PublishConfigRequest4.copy(namespaceId, request);
		String key;
		if (request.isBeta()) {
			key = TransactionUtils.buildTransactionKey(
					PropertiesEnum.InterestKey.CONFIG_DATA,
					namespaceId, request.getGroupId(), request.getDataId());
		}
		else {
			key = TransactionUtils.buildTransactionKey(
					PropertiesEnum.InterestKey.CONFIG_DATA,
					namespaceId, request.getGroupId(), request.getDataId());
		}
		Datum datum = new Datum(key, GsonUtils.toJsonBytes(request4),
				PublishConfigRequest4.CLASS_NAME);
		datum.setOperation(modifyConfig);
		return commit(datum);
	}

	@Override
	public ResponseData<?> removeConfig(String namespaceId, DeleteConfigRequest request) {
		DeleteConfigRequest4 request4 = DeleteConfigRequest4.copy(namespaceId, request);
		String key;
		if (request.isBeta()) {
			key = TransactionUtils.buildTransactionKey(
					PropertiesEnum.InterestKey.CONFIG_DATA,
					namespaceId, request.getGroupId(), request.getDataId());
		}
		else {
			key = TransactionUtils.buildTransactionKey(
					PropertiesEnum.InterestKey.CONFIG_DATA,
					namespaceId, request.getGroupId(), request.getDataId());
		}
		Datum datum = new Datum(key, GsonUtils.toJsonBytes(request4),
				DeleteConfigRequest4.CLASS_NAME);
		datum.setOperation(deleteConfig);
		return commit(datum);
	}

	@Override
	public ResponseData<?> saveConfigHistory(String namespaceId,
			PublishConfigHistory request) {
		request.setNamespaceId(namespaceId);
		String key = TransactionUtils.buildTransactionKey(
				PropertiesEnum.InterestKey.CONFIG_DATA, BzConstants.CONFIG_INFO_HISTORY,
				namespaceId, request.getGroupId(), request.getDataId());
		Datum datum = new Datum(key, GsonUtils.toJsonBytes(request),
				PublishConfigHistory.CLASS_NAME);
		datum.setOperation(createConfigHistory);
		return commit(datum);
	}

	@Override
	public ResponseData<?> removeConfigHistory(String namespaceId,
			DeleteConfigHistory request) {
		request.setNamespaceId(namespaceId);
		String key = TransactionUtils.buildTransactionKey(
				PropertiesEnum.InterestKey.CONFIG_DATA,
				namespaceId, request.getGroupId(), request.getDataId());
		Datum datum = new Datum(key, GsonUtils.toJsonBytes(request),
				DeleteConfigHistory.CLASS_NAME);
		datum.setOperation(deleteConfigHistory);
		return commit(datum);
	}

	@Override
	public ResponseData<ConfigListVO> configList(ConfigQueryPage queryPage) {
		List<Map<String, String>> list = persistentHandler.configList(queryPage);
		list = CollectionUtils.isEmpty(list) ? Collections.emptyList() : list;
		return ResponseData.success(VOUtils.convertToConfigListVO(list));
	}

	@Override
	public ResponseData<ConfigDetailVO> configDetail(String namespaceId, String groupId,
			String dataId) {
		ConfigInfoDTO configInfoDTO = persistentHandler.configDetail(namespaceId, groupId,
				dataId);
		if (configInfoDTO != null) {
			if (configInfoDTO instanceof ConfigBetaInfoDTO) {
				return ResponseData.success(VOUtils
						.convertToConfigDetailVO((ConfigBetaInfoDTO) configInfoDTO));
			}
			return ResponseData.success(VOUtils.convertToConfigDetailVO(configInfoDTO));
		}
		throw new NotThisResourceException();
	}

	private void publishEvent(ConfigChangeEvent source) {
		changeEventDisruptor.publishEvent((target, sequence) -> {
			source.setSequence(sequence);
			target.rest();
			target.setEvent(source);
		});
	}

	private ConfigChangeEvent buildConfigChangeEvent(String namespaceId,
			BaseConfigRequest request, String content, String encryption,
			EventType type) {
		return ConfigChangeEvent.builder().namespaceId(namespaceId)
				.dataId(request.getDataId()).groupId(request.getGroupId())
				.content(content).encryption(encryption).source(this).eventType(type)
				.build();
	}

	private ResponseData<?> commit(Datum datum) {
		datum.setBz(PropertiesEnum.Bz.CONFIG.name());
		CompletableFuture<ResponseData<Boolean>> future = clusterManager.commit(datum,
				failCallback);
		try {
			ResponseData<Boolean> data = future.get(10_000L, TimeUnit.MILLISECONDS);
			return data;
		}
		catch (InterruptedException | ExecutionException | TimeoutException e) {
			return ResponseData.fail(e);
		}
	}

	private TransactionConsumer<Transaction> publishConsumer() {
		return new TransactionConsumer<Transaction>() {
			@Override
			public void accept(Transaction transaction) throws Throwable {
				PublishConfigRequest4 request4 = GsonUtils.toObj(transaction.getData(),
						PublishConfigRequest4.class);
				String namespace = request4.getNamespaceId();
				if (Objects.isNull(
						namespaceService.findOneNamespaceByName(namespace).getData())) {
					namespaceService.createNamespace(
							NamespaceRequest.builder().namespace(namespace).build());
				}
				request4.setAttribute("id", transaction.getId());
				if (persistentHandler.saveConfigInfo(request4.getNamespaceId(), request4)
						&& request4.getStatus() == PropertiesEnum.ConfigStatus.PUBLISH
								.getStatus()) {
					ConfigChangeEvent event = ConfigOperationService.this
							.buildConfigChangeEvent(request4.getNamespaceId(), request4,
									request4.getContent(), request4.getEncryption(),
									EventType.PUBLISH);
					event.setBeta(request4.isBeta());
					if (request4.isBeta()) {
						event.setClientIps(request4.getClientIps());
					}
					event.setConfigType(request4.getType());
					ConfigOperationService.this.publishEvent(event);
				}
			}

			@Override
			public void onError(TransactionException te) {
			}
		};
	}

	private TransactionConsumer<Transaction> modifyConsumer() {
		return new TransactionConsumer<Transaction>() {
			@Override
			public void accept(Transaction transaction) throws Throwable {
				PublishConfigRequest4 request4 = GsonUtils.toObj(transaction.getData(),
						PublishConfigRequest4.class);
				String namespace = request4.getNamespaceId();
				if (Objects.isNull(
						namespaceService.findOneNamespaceByName(namespace).getData())) {
					throw new NotThisResourceException(
							"No resources in the namespace ：" + namespace);
				}
				request4.setAttribute("isLeader", clusterManager.isLeader());
				if (persistentHandler.modifyConfigInfo(request4.getNamespaceId(),
						request4)) {
					ConfigChangeEvent event = buildConfigChangeEvent(
							request4.getNamespaceId(), request4, request4.getContent(),
							request4.getEncryption(), EventType.MODIFIED);
					event.setBeta(request4.isBeta());
					if (request4.isBeta()) {
						event.setClientIps(request4.getClientIps());
					}
					event.setConfigType(request4.getType());
					publishEvent(event);
				}
			}

			@Override
			public void onError(TransactionException te) {
			}
		};
	}

	private TransactionConsumer<Transaction> deleteConsumer() {
		return new TransactionConsumer<Transaction>() {
			@Override
			public void accept(Transaction transaction) throws Throwable {
				DeleteConfigRequest4 request4 = GsonUtils.toObj(transaction.getData(),
						DeleteConfigRequest4.class);
				if (persistentHandler.removeConfigInfo(request4.getNamespaceId(),
						request4)) {
					ConfigChangeEvent event = buildConfigChangeEvent(
							request4.getNamespaceId(), request4, "", "",
							EventType.DELETE);
					publishEvent(event);
				}
			}

			@Override
			public void onError(TransactionException te) {
			}
		};

	}

	private TransactionConsumer<Transaction> saveConfigHistoryConsumer() {
		return new TransactionConsumer<Transaction>() {
			@Override
			public void accept(Transaction transaction) throws Throwable {
				PublishConfigHistory history = GsonUtils.toObj(transaction.getData(),
						PublishConfigHistory.class);
				history.setAttribute("id", transaction.getId());
				persistentHandler.saveConfigHistory(history.getNamespaceId(), history);
			}

			@Override
			public void onError(TransactionException te) {

			}
		};
	}

	private TransactionConsumer<Transaction> deleteConfigHistoryConsumer() {
		return new TransactionConsumer<Transaction>() {
			@Override
			public void accept(Transaction transaction) throws Throwable {
				DeleteConfigHistory history = GsonUtils.toObj(transaction.getData(),
						DeleteConfigHistory.class);
				persistentHandler.removeConfigHistory(history.getNamespaceId(), history);
			}

			@Override
			public void onError(TransactionException te) {

			}
		};
	}

	@Override
	public void onEvent(ConfigChangeEventHandler eventHandler) throws Exception {
		try {
			log.info("Begin Dump config-info to file : {}", eventHandler.getEvent());
			ConfigChangeEvent event = eventHandler.getEvent();
			if (EventType.PUBLISH.compareTo(event.getEventType()) == 0) {
				configCacheItemManager.registerConfigCacheItem(event.getNamespaceId(),
						event);
			}
			if (EventType.DELETE.compareTo(event.getEventType()) == 0) {
				configCacheItemManager.deregisterConfigCacheItem(event.getNamespaceId(),
						event.getGroupId(), event.getDataId());
				log.info("remove config");
				return;
			}
			configCacheItemManager.updateContent(event.getNamespaceId(), event);
			NotifyEvent source = NotifyEvent.builder().namespaceId(event.getNamespaceId())
					.groupId(event.getGroupId()).dataId(event.getDataId())
					.eventType(event.getEventType()).entryption(event.getEncryption())
					.build();
			notifyEventDisruptor.publishEvent((target, sequence) -> {
				source.setSequence(sequence);
				target.rest();
				target.setEvent(source);
			});
		}
		catch (Exception e) {
			log.error("notify ConfigChangeEvent has some error : {0}", e);
		}
	}

	@Override
	public void onNotify(Occurrence occurrence, Publisher publisher) {
		Object event = occurrence.getOrigin();
		Optional<CompletableFuture<Boolean>> futureOpt = occurrence.getAnswer();
		futureOpt.ifPresent(future -> {
			future.complete(true);
			if (event instanceof PublishConfigHistory) {
				PublishConfigHistory request = (PublishConfigHistory) event;
				saveConfigHistory(request.getNamespaceId(), request);
				return;
			}
			if (event instanceof DeleteConfigHistory) {
				DeleteConfigHistory request = (DeleteConfigHistory) event;
				removeConfigHistory(request.getNamespaceId(), request);
			}
		});

	}
}
