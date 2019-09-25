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
package com.lessspring.org.watch;

import com.google.gson.reflect.TypeToken;
import com.lessspring.org.AbstractListener;
import com.lessspring.org.CacheConfigManager;
import com.lessspring.org.Configuration;
import com.lessspring.org.LifeCycle;
import com.lessspring.org.NameUtils;
import com.lessspring.org.api.ApiConstant;
import com.lessspring.org.api.Code;
import com.lessspring.org.http.HttpClient;
import com.lessspring.org.http.impl.EventReceiver;
import com.lessspring.org.http.param.Body;
import com.lessspring.org.http.param.Header;
import com.lessspring.org.http.param.Query;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.model.vo.WatchRequest;
import com.lessspring.org.model.vo.WatchResponse;
import com.lessspring.org.pojo.CacheItem;
import com.lessspring.org.utils.MD5Utils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class WatchConfigWorker implements LifeCycle {

    private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            new ThreadFactory() {

                AtomicInteger id = new AtomicInteger(0);

                @Override
                public Thread newThread(@NotNull Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setDaemon(true);
                    thread.setName("com.lessspring.org.watcher-" + id.getAndIncrement());
                    return thread;
                }
            });

    private Map<String, CacheItem> cacheItemMap;
    private CacheConfigManager configManager;
    private final HttpClient httpClient;
    private final Configuration configuration;
    private EventReceiver<WatchResponse> receiver;

    public WatchConfigWorker(HttpClient httpClient, Configuration configuration) {
        this.httpClient = httpClient;
        this.configuration = configuration;
    }

    @Override
    public void init() {
        this.cacheItemMap = new ConcurrentHashMap<>(16);
    }

    public void setConfigManager(CacheConfigManager configManager) {
        this.configManager = configManager;
        executor.schedule(this::createWatcher, 1000, TimeUnit.MILLISECONDS);
    }

    public void registerListener(String groupId, String dataId, AbstractListener listener) {
        CacheItem cacheItem = computeIfAbsentCacheItem(groupId, dataId);
        cacheItem.addListener(listener);
        onChange();
    }

    public void deregisterListener(String groupId, String dataId, AbstractListener listener) {
        CacheItem cacheItem = getCacheItem(groupId, dataId);
        cacheItem.removeListener(listener);
    }

    public void notifyWatcher(final String groupId, final String dataId, final String content, final String type) {
        String key = NameUtils.buildName(groupId, dataId);
        Optional<List<AbstractListener>> listeners = Optional.ofNullable(cacheItemMap.get(key).listListener());
        listeners.ifPresent(abstractListeners -> {
            for (AbstractListener listener : abstractListeners) {
                Runnable job = () -> listener.onReceive(new ConfigInfo(groupId, dataId, content, type));
                Executor userExecutor = listener.executor();
                if (Objects.isNull(userExecutor)) {
                    job.run();
                } else {
                    userExecutor.execute(job);
                }
            }
        });
    }

    @Override
    public void destroy() {
        receiver.cancle();
        receiver = null;
        configManager = null;
        httpClient.destroy();
        executor.shutdown();
    }

    private void onChange() {
        if (Objects.nonNull(receiver)) {
            receiver.cancle();
            receiver = null;
        }
        executor.schedule(this::createWatcher, 1000, TimeUnit.MILLISECONDS);
    }

    private void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    private CacheItem computeIfAbsentCacheItem(String groupId, String dataId) {
        final String key = NameUtils.buildName(groupId, dataId);
        Supplier<CacheItem> supplier = () -> CacheItem.builder()
                .withGroupId(groupId)
                .withDataId(dataId)
                .withLastMd5("")
                .build();
        cacheItemMap.computeIfAbsent(key, s -> supplier.get());
        return cacheItemMap.get(key);
    }

    private CacheItem getCacheItem(String groupId, String dataId) {
        final String key = NameUtils.buildName(groupId, dataId);
        return cacheItemMap.get(key);
    }

    private void updateAndNotify(String groupId, String dataId, String content, String type) {
        final String key = NameUtils.buildName(groupId, dataId);
        final String lastMd5 = MD5Utils.md5Hex(content);
        final CacheItem oldItem = cacheItemMap.get(key);
        if (Objects.nonNull(oldItem) && oldItem.isChange(lastMd5)) {
            oldItem.setLastMd5(lastMd5);
            notifyWatcher(groupId, dataId, content, type);
        }
    }

    private void removeCacheItem(String groupId, String dataId) {
        String key = NameUtils.buildName(groupId, dataId);
        if (cacheItemMap.containsKey(key)) {
            cacheItemMap.remove(key);
            onChange();
        }
    }

    public Map<String, CacheItem> copy() {
        return new HashMap<>(cacheItemMap);
    }

    private void createWatcher() {
        Map<String, CacheItem> tmp = copy();
        Map<String, String> watchInfo = tmp.entrySet()
                .stream()
                .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue().getLastMd5()), HashMap::putAll);
        final WatchRequest request = new WatchRequest(configuration.getNamespaceId(), watchInfo);
        final Body body = Body.objToBody(request);

        receiver = new EventReceiver<WatchResponse>() {

            @Override
            public void onReceive(WatchResponse data) {
                if (!data.isEmpty()) {
                    WatchConfigWorker.this.updateAndNotify(
                            data.getGroupId(),
                            data.getDataId(),
                            data.getContent(),
                            data.getType()
                    );
                }
            }

            @Override
            public void onError(Throwable throwable) {
                WatchConfigWorker.this.onError(throwable);
            }
        };
        try {
            final Header header = Header.newInstance()
                    .addParam("Accept", "text/event-stream")
                    .addParam("Cache-Control", "no-cache");
            httpClient.serverSendEvent(ApiConstant.WATCH_CONFIG, header, body, WatchResponse.class, receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
