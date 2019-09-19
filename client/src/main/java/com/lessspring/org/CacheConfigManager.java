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
package com.lessspring.org;

import com.google.common.eventbus.EventBus;
import com.lessspring.org.pojo.CacheItem;
import com.lessspring.org.utils.MD5Utils;
import com.lessspring.org.watch.WatchConfigWorker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class CacheConfigManager implements LifeCycle {

    private Map<String, CacheItem> cacheItemMap;

    private WatchConfigWorker worker;

    public CacheConfigManager(WatchConfigWorker worker) {
        this.worker = worker;
    }

    @Override
    public void init() {
        this.cacheItemMap = new ConcurrentHashMap<>(16);
        this.worker.setConfigManager(this);
    }

    public void addCacheItem(String groupId, String dataId, String content, String type) {
        String key = NameUtils.buildName(groupId, dataId);
        String md5 = MD5Utils.md5Hex(content);
        CacheItem oldItem = cacheItemMap.get(key);
        if (oldItem != null) {
            if (oldItem.isChange(md5)) {
                oldItem.setLastMd5(md5);
                worker.notifyWatcher(groupId, dataId, content, type);
                worker.onChange();
            }
        } else {
            CacheItem newItem = CacheItem.builder()
                    .withGroupId(groupId)
                    .withDataId(dataId)
                    .withLastMd5(md5)
                    .build();
            cacheItemMap.put(key, newItem);
            worker.notifyWatcher(groupId, dataId,  content, type);
            worker.onChange();
        }
    }

    public void removeCacheItem(String groupId, String dataId) {
        String key = NameUtils.buildName(groupId, dataId);
        cacheItemMap.remove(key);
        worker.onChange();
    }

    public Map<String, CacheItem> copy() {
        return new HashMap<>(cacheItemMap);
    }

    @Override
    public void destroy() {
        worker = null;
        cacheItemMap.clear();
    }
}
