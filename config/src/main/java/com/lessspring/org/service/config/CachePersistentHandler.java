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

import com.lessspring.org.DiskUtils;
import com.lessspring.org.NameUtils;
import com.lessspring.org.db.dto.ConfigInfoDTO;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.model.vo.BaseConfigRequest;
import com.lessspring.org.model.vo.DeleteConfigRequest;
import com.lessspring.org.model.vo.PublishConfigRequest;
import com.lessspring.org.pojo.CacheItem;
import com.lessspring.org.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component(value = "cachePersistentHandler")
public class CachePersistentHandler implements PersistentHandler {

    private final ConfigCacheItemManager configCacheItemManager;
    private final PersistentHandler persistentHandler;

    public CachePersistentHandler(ConfigCacheItemManager configCacheItemManager,
                                  @Qualifier(value = "persistentHandler") PersistentHandler persistentHandler) {
        this.configCacheItemManager = configCacheItemManager;
        this.persistentHandler = persistentHandler;
    }

    @Override
    public ConfigInfo readConfigContent(String namespaceId, BaseConfigRequest request) {
        CacheItem cacheItem = configCacheItemManager.queryCacheItem(namespaceId,
                request.getGroupId(), request.getDataId());
        final int lockResult = ConfigCacheItemManager.tryReadLock(cacheItem);
        assert (lockResult != 0);
        if (lockResult < 0) {
            log.warn("[dump-error] write lock failed. {}", cacheItem.getKey());
            return null;
        }
        ConfigInfo configInfo;
        try {
            String s = DiskUtils.readFile(namespaceId, NameUtils.buildName(request.getGroupId(),
                    request.getDataId()));
            // Directly read cache did not read to the configuration file,
            // read the database directly
            if (StringUtils.isEmpty(s)) {
                ConfigInfo infoDB = persistentHandler.readConfigContent(namespaceId, request);
                ConfigInfoDTO dto = request.getAttribute(ConfigInfoDTO.NAME);
                request.getAttributes().clear();
                configCacheItemManager.dumpConfig(namespaceId, dto);
                return infoDB;
            }
            configInfo = GsonUtils.toObj(s, ConfigInfo.class);
            configInfo.setEncryption("");
        } finally {
            ConfigCacheItemManager.releaseReadLock(cacheItem);
        }
        return configInfo;
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
        return persistentHandler.removeConfigInfo(namespaceId, request);
    }
}
