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
package com.lessspring.org.service.config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Supplier;

import com.lessspring.org.DiskUtils;
import com.lessspring.org.NameUtils;
import com.lessspring.org.db.dto.ConfigBetaInfoDTO;
import com.lessspring.org.db.dto.ConfigInfoDTO;
import com.lessspring.org.event.EventType;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.model.vo.BaseConfigRequest;
import com.lessspring.org.pojo.CacheItem;
import com.lessspring.org.pojo.event.ConfigChangeEvent;
import com.lessspring.org.service.config.PersistentHandler;
import com.lessspring.org.utils.GsonUtils;
import com.lessspring.org.utils.MD5Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component
public class ConfigCacheItemManager {

	private final Map<String, CacheItem> cacheItemMap = new ConcurrentHashMap<>(16);

	@Autowired
	private PersistentHandler persistentHandler;

	public ConfigCacheItemManager() {}

	public ConfigInfo loadConfigFromDB(final String namespaceId, final String groupId,
			final String dataId) {
		BaseConfigRequest request = BaseConfigRequest.builder().withGroupId(groupId)
				.withDataId(dataId).build();
		ConfigInfo configInfo = persistentHandler.readConfigContent(namespaceId, request);
		ConfigInfoDTO dto = request.getAttribute(ConfigInfoDTO.NAME);
		if (dto instanceof ConfigBetaInfoDTO) {
			dumpConfigBeta(namespaceId, (ConfigBetaInfoDTO) dto);
		}
		else {
			dumpConfig(namespaceId, dto);
		}
		return configInfo;
	}

	public void dumpConfig(final String namespaceId, final ConfigInfoDTO configInfoDTO) {
		ConfigChangeEvent event = ConfigChangeEvent.builder()
				.groupId(configInfoDTO.getGroupId()).dataId(configInfoDTO.getDataId())
				.content(new String(configInfoDTO.getContent(),
						Charset.forName(StandardCharsets.UTF_8.name())))
				.file(configInfoDTO.getFile()).fileSource(configInfoDTO.getFileSource())
				.configType(configInfoDTO.getType()).build();
		registerConfigCacheItem(namespaceId, event);
		updateContent(namespaceId, event);
	}

	public void dumpConfigBeta(final String namespaceId,
			final ConfigBetaInfoDTO betaInfoDTO) {
		ConfigChangeEvent event = ConfigChangeEvent.builder()
				.groupId(betaInfoDTO.getGroupId()).dataId(betaInfoDTO.getDataId())
				.content(new String(betaInfoDTO.getContent(),
						Charset.forName(StandardCharsets.UTF_8.name())))
				.file(betaInfoDTO.getFile()).fileSource(betaInfoDTO.getFileSource())
				.clientIps(betaInfoDTO.getClientIps()).configType(betaInfoDTO.getType())
				.build();
		registerConfigCacheItem(namespaceId, event);
		updateContent(namespaceId, event);
	}

	public void registerConfigCacheItem(final String namespaceId,
			final ConfigChangeEvent event) {
		final String key = NameUtils.buildName(namespaceId, event.getGroupId(),
				event.getDataId());
		Set<String> betaClientIps = new CopyOnWriteArraySet<>();
		for (String ip : event.getClientIps().split(",")) {
			betaClientIps.add(ip.trim());
		}
		Supplier<CacheItem> supplier = () -> {
			final CacheItem itemSave = new CacheItem(namespaceId, event.getGroupId(),
					event.getDataId(), event.isFile());
			if (event.isFile()) {
				itemSave.setLastMd5(MD5Utils.md5Hex(event.getFileSource()));
			}
			else {
				itemSave.setLastMd5(MD5Utils.md5Hex(event.getContent()));
			}
			itemSave.setBeta(event.isBeta());
			itemSave.setBetaClientIps(betaClientIps);
			return itemSave;
		};
		CacheItem item = cacheItemMap.putIfAbsent(key, supplier.get());
		if (Objects.nonNull(item)) {
			if (event.isFile()) {
				item.setLastMd5(MD5Utils.md5Hex(event.getFileSource()));
			}
			else {
				item.setLastMd5(MD5Utils.md5Hex(event.getContent()));
			}
			item.setBeta(event.isBeta());
			item.setBetaClientIps(betaClientIps);
		}
	}

	public void deregisterConfigCacheItem(final String namespaceId,
			final ConfigChangeEvent event) {
		final String key = NameUtils.buildName(namespaceId, event.getGroupId(),
				event.getDataId());
		cacheItemMap.remove(key);
	}

	public CacheItem queryCacheItem(final String namespaceId, final String groupId,
			final String dataId) {
		final String key = NameUtils.buildName(namespaceId, groupId, dataId);
		CacheItem item = cacheItemMap.get(key);
		if (Objects.nonNull(item)) {
			return item;
		}
		CacheItem tmp = new CacheItem(namespaceId, groupId, dataId, false);
		item = cacheItemMap.putIfAbsent(key, tmp);
		return (null == item) ? tmp : item;
	}

	public boolean updateContent(final String namespaceId,
			final ConfigChangeEvent event) {
		final CacheItem cacheItem = queryCacheItem(namespaceId, event.getGroupId(),
				event.getDataId());
		event.setEncryption(StringUtils.EMPTY);
		final int lockResult = tryWriteLock(cacheItem);
		assert (lockResult != 0);
		if (lockResult < 0) {
			log.warn("[dump-error] write lock failed. {}", cacheItem.getKey());
			return false;
		}
		try {
			final String groupId = event.getGroupId();
			final String dataId = event.getDataId();
			if (Objects.equals(EventType.DELETE, event.getEventType())) {
				DiskUtils.deleteFile(namespaceId, NameUtils.buildName(groupId, dataId));
				return true;
			}
			final ConfigInfo configInfo;
			if (event.isFile()) {
				configInfo = new ConfigInfo(groupId, dataId, event.getFileSource(),
						event.getConfigType(), event.getEncryption());
				cacheItem.setLastMd5(MD5Utils.md5Hex(event.getFileSource()));
			}
			else {
				configInfo = new ConfigInfo(groupId, dataId, event.getContent(),
						event.getConfigType(), event.getEncryption());
				cacheItem.setLastMd5(MD5Utils.md5Hex(event.getContent()));
			}
			DiskUtils.writeFile(namespaceId, NameUtils.buildName(groupId, dataId),
					GsonUtils.toJsonBytes(configInfo));
			cacheItem.setLastUpdateTime(System.currentTimeMillis());
		}
		catch (Exception e) {
			log.error("update config content has some error : {}", e);
		}
		finally {
			releaseWriteLock(cacheItem);
		}
		return true;
	}

	public static int tryReadLock(CacheItem cacheItem) {
		int result = (cacheItem.tryReadLock() ? 1 : -1);
		if (result < 0) {
			log.warn("[read-lock] failed, {}, {}", result, cacheItem.getKey());
		}
		return result;
	}

	public static void releaseReadLock(CacheItem cacheItem) {
		cacheItem.releaseReadLock();
	}

	public static int tryWriteLock(CacheItem cacheItem) {
		int result = (cacheItem.tryWriteLock() ? 1 : -1);
		if (result < 0) {
			log.warn("[write-lock] failed, {}, {}", result, cacheItem.getKey());
		}
		return result;
	}

	public static void releaseWriteLock(CacheItem cacheItem) {
		cacheItem.releaseWriteLock();
	}

}
