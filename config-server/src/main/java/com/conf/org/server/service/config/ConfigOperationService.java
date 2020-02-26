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
package com.conf.org.server.service.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.conf.org.db.dto.ConfigBetaInfoDTO;
import com.conf.org.db.dto.ConfigInfoDTO;
import com.conf.org.event.EventType;
import com.conf.org.model.dto.ConfigInfo;
import com.conf.org.model.vo.BaseConfigRequest;
import com.conf.org.model.vo.ConfigQueryPage;
import com.conf.org.model.vo.DeleteConfigRequest;
import com.conf.org.model.vo.PublishConfigRequest;
import com.conf.org.model.vo.QueryConfigRequest;
import com.conf.org.model.vo.ResponseData;
import com.conf.org.raft.TransactionIdManager;
import com.conf.org.server.exception.NotThisResourceException;
import com.conf.org.server.service.cluster.ClusterManager;
import com.conf.org.server.service.cluster.FailCallback;
import com.conf.org.server.service.distributed.BaseTransactionCommitCallback;
import com.conf.org.server.pojo.event.config.ConfigChangeEvent;
import com.conf.org.server.pojo.event.config.ConfigChangeEventHandler;
import com.conf.org.server.pojo.event.config.NotifyEvent;
import com.conf.org.server.pojo.event.config.NotifyEventHandler;
import com.conf.org.server.pojo.request.DeleteConfigHistory;
import com.conf.org.server.pojo.request.NamespaceRequest;
import com.conf.org.server.pojo.vo.ConfigDetailVO;
import com.conf.org.server.pojo.vo.ConfigListVO;
import com.conf.org.server.service.publish.AbstractNotifyServiceImpl;
import com.conf.org.server.utils.DisruptorFactory;
import com.conf.org.server.utils.PropertiesEnum;
import com.conf.org.server.utils.VOUtils;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Service
public class ConfigOperationService
		implements OperationService, WorkHandler<ConfigChangeEventHandler> {

	private final NamespaceService namespaceService;
	private final Disruptor<ConfigChangeEventHandler> changeEventDisruptor;
	private final Disruptor<NotifyEventHandler> notifyEventDisruptor;
	private final PersistentHandler persistentHandler;
	private final BaseTransactionCommitCallback commitCallback;
	private final ClusterManager clusterManager;
	private FailCallback failCallback;
	private ConfigCacheItemManager configCacheItemManager;

	@Autowired
	private TransactionIdManager idManager;

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
	}

	@PostConstruct
	public void init() {
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
		if (Objects
				.isNull(namespaceService.findOneNamespaceByName(namespaceId).getData())) {
			namespaceService.createNamespace(
					NamespaceRequest.builder().namespace(namespaceId).build());
		}
		if (persistentHandler.saveConfigInfo(namespaceId, request) && request
				.getStatus() == PropertiesEnum.ConfigStatus.PUBLISH.getStatus()) {
			ConfigChangeEvent event = ConfigOperationService.this.buildConfigChangeEvent(
					namespaceId, request, request.getContent(), request.getEncryption(),
					EventType.PUBLISH);
			event.setBeta(request.isBeta());
			if (request.isBeta()) {
				event.setClientIps(request.getClientIps());
			}
			event.setConfigType(request.getType());
			ConfigOperationService.this.publishEvent(event);
		}
		return ResponseData.success();
	}

	@Override
	public ResponseData<?> modifyConfig(String namespaceId,
			PublishConfigRequest request) {
		if (Objects
				.isNull(namespaceService.findOneNamespaceByName(namespaceId).getData())) {
			throw new NotThisResourceException(
					"No resources in the namespace ：" + namespaceId);
		}
		request.setAttribute("isLeader", clusterManager.isLeader());
		if (persistentHandler.modifyConfigInfo(namespaceId, request)) {
			ConfigChangeEvent event = buildConfigChangeEvent(namespaceId, request,
					request.getContent(), request.getEncryption(), EventType.MODIFIED);
			event.setBeta(request.isBeta());
			if (request.isBeta()) {
				event.setClientIps(request.getClientIps());
			}
			event.setConfigType(request.getType());
			publishEvent(event);
		}
		return ResponseData.success();
	}

	@Override
	public ResponseData<?> removeConfig(String namespaceId, DeleteConfigRequest request) {
		if (persistentHandler.removeConfigInfo(namespaceId, request)) {
			ConfigChangeEvent event = buildConfigChangeEvent(namespaceId, request, "", "",
					EventType.DELETE);
			publishEvent(event);
		}
		return ResponseData.success();
	}

	@Override
	public ResponseData<?> removeConfigHistory(String namespaceId,
			DeleteConfigHistory request) {
		persistentHandler.removeConfigHistory(namespaceId, request);
		return ResponseData.success();
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

}
