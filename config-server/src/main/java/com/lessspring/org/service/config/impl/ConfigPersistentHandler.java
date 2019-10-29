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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import com.lessspring.org.ByteUtils;
import com.lessspring.org.db.dto.ConfigBetaInfoDTO;
import com.lessspring.org.db.dto.ConfigInfoDTO;
import com.lessspring.org.event.EventType;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.model.vo.BaseConfigRequest;
import com.lessspring.org.model.vo.DeleteConfigRequest;
import com.lessspring.org.model.vo.PublishConfigRequest;
import com.lessspring.org.pojo.event.ConfigChangeEvent;
import com.lessspring.org.pojo.event.NotifyEvent;
import com.lessspring.org.pojo.query.QueryConfigInfo;
import com.lessspring.org.repository.ConfigInfoHistoryMapper;
import com.lessspring.org.repository.ConfigInfoMapper;
import com.lessspring.org.service.config.ConfigCacheItemManager;
import com.lessspring.org.service.config.PersistentHandler;
import com.lessspring.org.service.publish.WatchClientManager;
import com.lessspring.org.utils.ConfigRequestUtils;
import com.lessspring.org.utils.DisruptorFactory;
import com.lessspring.org.utils.PropertiesEnum;
import com.lessspring.org.utils.SystemEnv;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component(value = "persistentHandler")
public class ConfigPersistentHandler
		implements PersistentHandler, WorkHandler<ConfigChangeEvent> {

	private final Disruptor<NotifyEvent> disruptorHolder;

	@Autowired
	@Lazy
	private ConfigCacheItemManager configCacheItemManager;

	@Resource
	private ConfigInfoMapper configInfoMapper;

	@Resource
	private ConfigInfoHistoryMapper historyMapper;

	@Resource
	private TransactionTemplate transactionTemplate;

	private final SystemEnv systemEnv = SystemEnv.getSingleton();

	public ConfigPersistentHandler(WatchClientManager watchClientManager) {
		disruptorHolder = DisruptorFactory.build(NotifyEvent::new,
				"Notify-Event-Disruptor");
		disruptorHolder.handleEventsWithWorkerPool(watchClientManager);
		disruptorHolder.start();
	}

	@PreDestroy
	public void shutdown() {
		disruptorHolder.shutdown();
	}

	@Transactional(readOnly = true)
	@Override
	public ConfigInfo readConfigContent(String namespaceId, BaseConfigRequest request) {
		final String dataId = request.getDataId();
		final String groupId = request.getGroupId();
		QueryConfigInfo queryConfigInfo = QueryConfigInfo.builder()
				.namespaceId(namespaceId).groupId(groupId).dataId(dataId).build();
		ConfigInfoDTO dto = configInfoMapper.findConfigInfo(queryConfigInfo);
		if (dto == null) {
			dto = configInfoMapper.findConfigBetaInfo(queryConfigInfo);
		}
		if (dto == null) {
			return null;
		}
		byte[] origin = dto.getContent();
		// unable transport config-context encryption token
		request.setAttribute(ConfigInfoDTO.NAME, dto);
		ConfigInfo info = ConfigInfo.builder().groupId(dto.getGroupId())
				.dataId(dto.getDataId()).file(dto.getFileSource()).type(dto.getType())
				.build();
		byte type = ByteUtils.getByteByIndex(origin, 0);
		byte[] source = ByteUtils.cut(origin, 1, origin.length - 1);
		if (Objects.equals(type, PropertiesEnum.ConfigType.FILE.getType())) {
			info.setFile(source);
		}
		else {
			info.setContent(
					new String(source, Charset.forName(StandardCharsets.UTF_8.name())));
		}
		return info;
	}

	@Override
	public boolean saveConfigInfo(String namespaceId, PublishConfigRequest request) {
		int affect = -1;
		byte[] save = ConfigRequestUtils.getByte(request);
		final long id = request.getAttribute("id");
		if (request.isBeta()) {
			ConfigBetaInfoDTO infoDTO = ConfigBetaInfoDTO.sbuilder().id(id)
					.namespaceId(namespaceId).groupId(request.getGroupId())
					.dataId(request.getDataId()).content(save).type(request.getType())
					.clientIps(request.getClientIps())
					.createTime(System.currentTimeMillis()).build();
			affect = configInfoMapper.saveConfigBetaInfo(infoDTO);
		}
		else {
			ConfigInfoDTO infoDTO = ConfigInfoDTO.builder().id(id)
					.namespaceId(namespaceId).groupId(request.getGroupId())
					.dataId(request.getDataId()).content(save).type(request.getType())
					.createTime(System.currentTimeMillis()).build();
			affect = configInfoMapper.saveConfigInfo(infoDTO);
		}
		log.debug("save config-success, affect rows is : {}, primary key is : {}", affect,
				id);
		return true;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public boolean modifyConfigInfo(String namespaceId, PublishConfigRequest request) {
		int affect = -1;
		byte[] save = ConfigRequestUtils.getByte(request);
		if (request.isBeta()) {
			// The smallest atomic operation, no need for transaction rollback operation
			ConfigBetaInfoDTO infoDTO = ConfigBetaInfoDTO.sbuilder()
					.namespaceId(namespaceId).groupId(request.getGroupId())
					.dataId(request.getDataId()).content(save).type(request.getType())
					.clientIps(request.getClientIps()).build();
			affect = configInfoMapper.updateConfigBetaInfo(infoDTO);
		}
		else {
			// General configuration when the update, will automatically save the
			// configuration record history
			final QueryConfigInfo queryConfigInfo = QueryConfigInfo.builder()
					.namespaceId(namespaceId).groupId(request.getGroupId())
					.dataId(request.getDataId()).build();
			// ConfigInfoDTO old = configInfoMapper.findConfigInfo(queryConfigInfo);
			// ConfigInfoHistoryDTO history = new ConfigInfoHistoryDTO();
			// DBUtils.changeConfigInfo2History(old, history);
			// historyMapper.save(history);
			ConfigInfoDTO infoDTO = ConfigInfoDTO.builder().namespaceId(namespaceId)
					.groupId(request.getGroupId()).dataId(request.getDataId())
					.content(save).type(request.getType()).build();
			affect = configInfoMapper.updateConfigInfo(infoDTO);
		}
		log.debug("modify config-success, affect rows is : {}", affect);
		return true;
	}

	@Override
	public boolean removeConfigInfo(String namespaceId, DeleteConfigRequest request) {
		if (request.isBeta()) {
			configInfoMapper.removeConfigBetaInfo(request);
		}
		else {
			configInfoMapper.removeConfigInfo(request);
		}
		return true;
	}

	@Override
	public void onEvent(ConfigChangeEvent event) throws Exception {
		try {
			if (EventType.PUBLISH.compareTo(event.getEventType()) == 0) {
				configCacheItemManager.registerConfigCacheItem(event.getNamespaceId(),
						event);
			}
			if (EventType.DELETE.compareTo(event.getEventType()) == 0) {
				configCacheItemManager.deregisterConfigCacheItem(event.getNamespaceId(),
						event);
				return;
			}
			configCacheItemManager.updateContent(event.getNamespaceId(), event);
			NotifyEvent source = NotifyEvent.builder().namespaceId(event.getNamespaceId())
					.groupId(event.getGroupId()).dataId(event.getDataId())
					.eventType(event.getEventType()).entryption(event.getEncryption())
					.build();
			disruptorHolder.publishEvent(
					(target, sequence1) -> NotifyEvent.copy(sequence1, source, target));
		}
		catch (Exception e) {
			log.error("notify ConfigChangeEvent has some error : {0}", e);
		}
	}
}
