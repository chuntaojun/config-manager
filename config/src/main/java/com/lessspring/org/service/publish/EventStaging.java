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
package com.lessspring.org.service.publish;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.lessspring.org.NameUtils;
import com.lessspring.org.pojo.query.QueryConfigInfo;
import com.lessspring.org.repository.ConfigInfoMapper;
import com.lessspring.org.DiskUtils;
import com.lessspring.org.utils.MD5Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component
public class EventStaging {

    private final Cache<String, String> hotCache;

    @Resource
    private ConfigInfoMapper configInfoMapper;

    public EventStaging() {
        this.hotCache = CacheBuilder.newBuilder()
                .maximumSize(10_000)
                .build(
                        new CacheLoader<String, String>() {
                            @Override
                            public String load(@NotNull String key) throws Exception {
                                String[] keys = key.split(NameUtils.LINK_STRING);
                                final String namespaceId = keys[0];
                                final String groupId = keys[1];
                                final String dataId = keys[2];
                                String content = DiskUtils.readFile(namespaceId, NameUtils.buildName(groupId, dataId));
                                if (StringUtils.isEmpty(content)) {
                                    content = configInfoMapper.findConfigInfoContent(new QueryConfigInfo(namespaceId, groupId, dataId));
                                }
                                return MD5Utils.md5Hex(content);
                            }
                        });
    }

    public boolean invalidate(String key) {
        hotCache.invalidate(key);
        return true;
    }

    public boolean updateCache(String key, String value) {
        hotCache.put(key, value);
        return true;
    }

    public Map<String, Boolean> checkChange(Map<String, String> oldData) {
        return oldData.entrySet().stream()
                .filter(entry -> !Objects.equals(hotCache.getIfPresent(entry.getKey()), entry.getValue()))
                .collect(HashMap::new, (m, e) -> m.put(e.getKey(), true), HashMap::putAll);
    }

}
