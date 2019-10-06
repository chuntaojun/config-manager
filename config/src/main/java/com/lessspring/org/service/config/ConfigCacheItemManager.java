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
import com.lessspring.org.event.EventType;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.model.vo.PublishConfigRequest;
import com.lessspring.org.pojo.CacheItem;
import com.lessspring.org.pojo.event.ConfigChangeEvent;
import com.lessspring.org.utils.GsonUtils;
import com.lessspring.org.utils.MD5Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component
public class ConfigCacheItemManager {

    private final Map<String, CacheItem> cacheItemMap = new ConcurrentHashMap<>(16);

    public void registerConfigCacheItem(final String namespaceId, final ConfigChangeEvent event) {
        final String key = NameUtils.buildName(namespaceId, event.getGroupId(), event.getDataId());

        cacheItemMap.computeIfAbsent(key, s -> {
            Set<String> betaClientIps = new CopyOnWriteArraySet<>();
            for (String ip : event.getClientIps().split(",")) {
                betaClientIps.add(ip.trim());
            }
            final CacheItem item = new CacheItem(namespaceId, event.getGroupId(), event.getDataId(),
                    event.isFile());
            if (event.isFile()) {
                item.setLastMd5(MD5Utils.md5Hex(event.getFileSource()));
            } else {
                item.setLastMd5(MD5Utils.md5Hex(event.getContent()));
            }
            item.setBeta(event.isBeta());
            item.setBetaClientIps(betaClientIps);
            return item;
        });
    }

    public void deregisterConfigCacheItem(final String namespaceId, final ConfigChangeEvent event) {
        final String key = NameUtils.buildName(namespaceId, event.getGroupId(), event.getDataId());
        cacheItemMap.remove(key);
    }

    public Optional<CacheItem> queryCacheItem(final String namespaceId, final String groupId, final String dataId) {
        final String key = NameUtils.buildName(namespaceId, groupId, dataId);
        return Optional.ofNullable(cacheItemMap.get(key));
    }

    public boolean updateContent(final String namespaceId, final ConfigChangeEvent event) {
        Optional<CacheItem> optionalCacheItem = queryCacheItem(namespaceId, event.getGroupId(), event.getDataId());
        boolean[] result = new boolean[]{true};
        optionalCacheItem.ifPresent(cacheItem -> {
            final int lockResult = tryWriteLock(cacheItem);
            assert (lockResult != 0);
            if (lockResult < 0) {
                log.warn("[dump-error] write lock failed. {}", cacheItem.getKey());
                result[0] = false;
                return;
            }
            try {
                final String groupId = event.getGroupId();
                final String dataId = event.getDataId();
                if (Objects.equals(EventType.DELETE, event.getEventType())) {
                    DiskUtils.deleteFile(namespaceId, NameUtils.buildName(groupId, dataId));
                    return;
                }
                final ConfigInfo configInfo;
                if (event.isFile()) {
                    configInfo = new ConfigInfo(groupId, dataId, event.getFileSource(), event.getConfigType(), event.getEncryption());
                    cacheItem.setLastMd5(MD5Utils.md5Hex(event.getFileSource()));
                } else {
                    configInfo = new ConfigInfo(groupId, dataId, event.getContent(), event.getConfigType(), event.getEncryption());
                    cacheItem.setLastMd5(MD5Utils.md5Hex(event.getContent()));
                }
                DiskUtils.writeFile(namespaceId, NameUtils.buildName(groupId, dataId), GsonUtils.toJsonBytes(configInfo));
                cacheItem.setLastUpdateTime(System.currentTimeMillis());
            } catch (Exception e) {
                log.error("update config content has some error : {}", e);
            } finally {
                releaseWriteLock(cacheItem);
            }
        });
        return optionalCacheItem.isPresent() && result[0];
    }

    static public int tryReadLock(CacheItem cacheItem) {
        int result = (cacheItem.tryReadLock() ? 1 : -1);
        if (result < 0) {
            log.warn("[read-lock] failed, {}, {}", result, cacheItem.getKey());
        }
        return result;
    }

    private static void releaseReadLock(CacheItem cacheItem) {
        cacheItem.releaseReadLock();
    }

    private static int tryWriteLock(CacheItem cacheItem) {
        int result = (cacheItem.tryWriteLock() ? 1 : -1);
        if (result < 0) {
            log.warn("[write-lock] failed, {}, {}", result, cacheItem.getKey());
        }
        return result;
    }

    private static void releaseWriteLock(CacheItem cacheItem) {
        cacheItem.releaseWriteLock();
    }

}
