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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;

import com.conf.org.db.dto.ConfigBetaInfoDTO;
import com.conf.org.db.dto.ConfigInfoDTO;
import com.conf.org.db.dto.ConfigInfoHistoryDTO;
import com.conf.org.model.dto.ConfigInfo;
import com.conf.org.model.vo.BaseConfigRequest;
import com.conf.org.model.vo.ConfigQueryPage;
import com.conf.org.model.vo.DeleteConfigRequest;
import com.conf.org.model.vo.PublishConfigRequest;
import com.conf.org.server.repository.ConfigInfoHistoryMapper;
import com.conf.org.server.repository.ConfigInfoMapper;
import com.conf.org.utils.ByteUtils;
import com.conf.org.server.pojo.query.QueryConfigInfo;
import com.conf.org.server.pojo.request.DeleteConfigHistory;
import com.conf.org.server.service.config.AbstractPersistentHandler;
import com.conf.org.server.utils.ConfigRequestUtils;
import com.conf.org.server.utils.PropertiesEnum;
import com.conf.org.server.utils.SystemEnv;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component(value = "persistentHandler")
public class ConfigPersistentHandler extends AbstractPersistentHandler {

	private final SystemEnv systemEnv = SystemEnv.getSingleton();
	@Resource
	private ConfigInfoMapper configInfoMapper;
	@Resource
	private ConfigInfoHistoryMapper historyMapper;
	@Resource
	private TransactionTemplate transactionTemplate;

	@Override
	public ConfigInfoDTO configDetail(String namespaceId, String groupId, String dataId) {
		QueryConfigInfo queryConfigInfo = QueryConfigInfo.builder()
				.namespaceId(namespaceId).groupId(groupId).dataId(dataId).build();
		ConfigInfoDTO dto = configInfoMapper.findConfigInfo(queryConfigInfo);
		if (dto == null) {
			dto = configInfoMapper.findConfigBetaInfo(queryConfigInfo);
		}
		return dto;
	}

	@Override
	public List<Map<String, String>> configList(ConfigQueryPage queryPage) {
		List<Map<String, String>> result = configInfoMapper.configList(queryPage);
		return Objects.isNull(result) ? Collections.emptyList() : result;
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
			info.setContent(ByteUtils.toString(source));
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
					.clientIps(request.getClientIps()).status(request.getStatus())
					.createTime(System.currentTimeMillis()).build();
			affect = configInfoMapper.saveConfigBetaInfo(infoDTO);
			request.setAttribute(ConfigBetaInfoDTO.NAME, infoDTO);
		}
		else {
			ConfigInfoDTO infoDTO = ConfigInfoDTO.builder().id(id)
					.namespaceId(namespaceId).groupId(request.getGroupId())
					.dataId(request.getDataId()).content(save).type(request.getType())
					.status(request.getStatus()).createTime(System.currentTimeMillis())
					.build();
			affect = configInfoMapper.saveConfigInfo(infoDTO);
			request.setAttribute(ConfigInfoDTO.NAME, infoDTO);
		}
		log.debug("save config-success, affect rows is : {}, primary key is : {}", affect,
				id);
		return true;
	}

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
			ConfigInfoDTO old = configInfoMapper.findConfigInfo(queryConfigInfo);
			ConfigInfoDTO infoDTO = ConfigInfoDTO.builder().namespaceId(namespaceId)
					.groupId(request.getGroupId()).dataId(request.getDataId())
					.content(save).type(request.getType()).version(old.getVersion() + 1)
					.build();
			affect = configInfoMapper.updateConfigInfo(infoDTO);

			ConfigInfoHistoryDTO historyDTO = ConfigInfoHistoryDTO.sbuilder()
					.namespaceId(namespaceId).groupId(old.getGroupId())
					.dataId(old.getDataId())
					.encryption(old.getEncryption())
					.lastModifyTime(System.currentTimeMillis())
					.content(old.getContent()).build();
			return historyMapper.save(historyDTO) != 0;
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
	public boolean removeConfigHistory(String namespaceId,
			DeleteConfigHistory deleteConfigHistory) {
		return historyMapper
				.batchDelete(Collections.singletonList(deleteConfigHistory.getId())) > 0;
	}

	@Override
	public int priority() {
		return LOW_PRIORITY;
	}
}
