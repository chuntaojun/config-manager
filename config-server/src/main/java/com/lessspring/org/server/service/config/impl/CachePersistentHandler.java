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
package com.lessspring.org.server.service.config.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.lessspring.org.db.dto.ConfigBetaInfoDTO;
import com.lessspring.org.db.dto.ConfigInfoDTO;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.model.vo.BaseConfigRequest;
import com.lessspring.org.model.vo.DeleteConfigRequest;
import com.lessspring.org.model.vo.PublishConfigRequest;
import com.lessspring.org.observer.Publisher;
import com.lessspring.org.server.pojo.CacheItem;
import com.lessspring.org.server.pojo.ReadWork;
import com.lessspring.org.server.pojo.WriteWork;
import com.lessspring.org.server.pojo.request.DeleteConfigHistory;
import com.lessspring.org.server.pojo.request.PublishConfigHistory;
import com.lessspring.org.server.service.config.AbstractPersistentHandler;
import com.lessspring.org.server.service.config.ConfigCacheItemManager;
import com.lessspring.org.server.utils.GsonUtils;
import com.lessspring.org.server.utils.SystemEnv;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * With the persistence of the processor cache function, for a read operation, to
 * intercept, read the file cache, if the file cache does not exist, is for the dump file
 * cache operation
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component(value = "cachePersistentHandler")
public class CachePersistentHandler extends AbstractPersistentHandler {

	private final SystemEnv systemEnv = SystemEnv.getSingleton();

	private final ConfigCacheItemManager configCacheItemManager;
	private final AbstractPersistentHandler persistentHandler;

	public CachePersistentHandler(ConfigCacheItemManager configCacheItemManager,
			@Qualifier(value = "persistentHandler") AbstractPersistentHandler persistentHandler) {
		this.configCacheItemManager = configCacheItemManager;
		this.persistentHandler = persistentHandler;
	}

	@Override
	public Publisher getPublisher() {
		return persistentHandler;
	}

	@Override
	public ConfigInfoDTO configDetail(String namespaceId, String groupId, String dataId) {
		return persistentHandler.configDetail(namespaceId, groupId, dataId);
	}

	@Override
	public List<Map<String, String>> configList(String namespaceId, long page,
			long pageSize, long lastId) {
		return persistentHandler.configList(namespaceId, page, pageSize, lastId);
	}

	@Override
	public ConfigInfo readConfigContent(String namespaceId, BaseConfigRequest request) {
		if (!systemEnv.isDumpToFile()) {
			return persistentHandler.readConfigContent(namespaceId, request);
		}
		final CacheItem cacheItem = configCacheItemManager.queryCacheItem(namespaceId,
				request.getGroupId(), request.getDataId());
		final ConfigInfo[] configInfo = new ConfigInfo[] { null };
		cacheItem.executeReadWork(new ReadWork() {
			@Override
			public void job() {
				String s = configCacheItemManager.readCacheFromDisk(namespaceId,
						request.getGroupId(), request.getDataId(), cacheItem.isBeta());
				// Directly read cache did not read to the configuration file,
				// read the database directly
				if (StringUtils.isEmpty(s)) {
					ConfigInfo infoDB = persistentHandler.readConfigContent(namespaceId,
							request);
					// After they perform query operations dto objects attached to the
					// attributes
					// Can't transfer the order
					ConfigInfoDTO dto = request.getAttribute(ConfigInfoDTO.NAME);
					if (Objects.isNull(dto)) {
						configInfo[0] = infoDB;
					}
					if (dto instanceof ConfigBetaInfoDTO) {
						configCacheItemManager.dumpConfigBeta(namespaceId,
								(ConfigBetaInfoDTO) dto);
					}
					else {
						configCacheItemManager.dumpConfig(namespaceId, dto);
					}
					configInfo[0] = infoDB;
				}
				else {
					configInfo[0] = GsonUtils.toObj(s, ConfigInfo.class);
				}
				if (configInfo[0] != null) {
					configInfo[0].setEncryption("");
					if (cacheItem.isBeta() && !cacheItem
							.canRead((String) request.getAttribute("clientIp"))) {
						configInfo[0] = null;
						log.debug(
								"this config-info is beta and this client : [{}] can't read",
								(String) request.getAttribute("clientIp"));
					}
					else {
						log.debug("config-info : {}", configInfo[0]);
					}
				}
				request.getAttributes().clear();
			}

			@Override
			public void onError(Exception exception) {
				configInfo[0] = null;
				log.error("Has some error : {0}", exception);
			}
		});
		return configInfo[0];
	}

	@Override
	public boolean saveConfigInfo(String namespaceId, PublishConfigRequest request) {
		return persistentHandler.saveConfigInfo(namespaceId, request);
	}

	@Override
	public boolean modifyConfigInfo(String namespaceId, PublishConfigRequest request) {
		return persistentHandler.modifyConfigInfo(namespaceId, request);
	}

	@Override
	public boolean removeConfigInfo(String namespaceId, DeleteConfigRequest request) {
		// Remove the configuration file, if open the file dump operation, the need to be
		// cleared
		if (systemEnv.isDumpToFile()) {
			final CacheItem cacheItem = configCacheItemManager.queryCacheItem(namespaceId,
					request.getGroupId(), request.getDataId());
			cacheItem.executeWriteWork(new WriteWork() {
				@Override
				public void job() {
					configCacheItemManager.removeCacheFromDisk(cacheItem.getNamespaceId(),
							cacheItem.getGroupId(), cacheItem.getDataId());
				}

				@Override
				public void onError(Exception exception) {

				}
			});
			// Cancel CacheItem manager
			configCacheItemManager.deregisterConfigCacheItem(cacheItem.getNamespaceId(),
					cacheItem.getGroupId(), cacheItem.getDataId());
		}
		return persistentHandler.removeConfigInfo(namespaceId, request);
	}

	@Override
	public boolean saveConfigHistory(String namespaceId,
			PublishConfigHistory publishConfigHistory) {
		return persistentHandler.saveConfigHistory(namespaceId, publishConfigHistory);
	}

	@Override
	public boolean removeConfigHistory(String namespaceId,
			DeleteConfigHistory deleteConfigHistory) {
		return persistentHandler.removeConfigHistory(namespaceId, deleteConfigHistory);
	}

	@Override
	public int priority() {
		return HIGH_PRIORITY / 2;
	}
}
