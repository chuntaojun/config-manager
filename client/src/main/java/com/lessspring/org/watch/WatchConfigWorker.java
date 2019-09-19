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

import com.google.common.eventbus.Subscribe;
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
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

    private Map<String, CopyOnWriteArrayList<AbstractListener>> watchListenerMap;
    private CacheConfigManager configManager;
    private final HttpClient httpClient;
    private final Configuration configuration;
    private EventReceiver<WatchResponse> receiver;

    public WatchConfigWorker(Configuration configuration, HttpClient httpClient) {
        this.configuration = configuration;
        this.httpClient = httpClient;
    }

    @Override
    public void init() {
        watchListenerMap = new ConcurrentHashMap<>(16);
    }

    public void setConfigManager(CacheConfigManager configManager) {
        this.configManager = configManager;
        executor.schedule(this::createWatcher, 5000, TimeUnit.MILLISECONDS);
    }

    public void registerListener(String groupId, String dataId, AbstractListener listener) {
        String key = NameUtils.buildName(groupId, dataId);
        watchListenerMap.computeIfAbsent(key, s -> new CopyOnWriteArrayList<>());
        watchListenerMap.get(key).add(listener);
    }

    public void deregisterListener(String groupId, String dataId, AbstractListener listener) {
        String key = NameUtils.buildName(groupId, dataId);
        if (watchListenerMap.containsKey(key)) {
            watchListenerMap.get(key).remove(listener);
        }
    }

    public void notifyWatcher(final String groupId, final String dataId, final String content, final String type) {
        String key = NameUtils.buildName(groupId, dataId);
        Optional<List<AbstractListener>> listeners = Optional.ofNullable(watchListenerMap.get(key));
        listeners.ifPresent(abstractListeners -> {
            for (AbstractListener listener : abstractListeners) {
                listener.onReceive(new ConfigInfo(content, type));
            }
        });
    }

    @Override
    public void destroy() {
        receiver.cancle();
        receiver = null;
        configManager = null;
        watchListenerMap.clear();
        httpClient.destroy();
        executor.shutdown();
    }

    public void onChange() {
        receiver.cancle();
        executor.schedule(this::createWatcher, 1000, TimeUnit.MILLISECONDS);
    }

    protected void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    private void createWatcher() {
        Map<String, CacheItem> tmp = configManager.copy();
        Map<String, String> watchInfo = tmp.entrySet()
                .stream()
                .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue().getLastMd5()), HashMap::putAll);
        WatchRequest request = new WatchRequest(configuration.getNamespaceId(), watchInfo);
        final Body body = Body.objToBody(request);

        receiver = new EventReceiver<WatchResponse>() {

            @Override
            public void onReceive(ResponseData<WatchResponse> data) {
                WatchResponse response = data.getData();
                if (data.getCode() == Code.SUCCESS.getCode()) {
                    WatchConfigWorker.this.notifyWatcher(
                            response.getGroupId(),
                            response.getDataId(),
                            response.getContent(),
                            response.getType());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                WatchConfigWorker.this.onError(throwable);
            }
        };

        httpClient.serverSendEvent(ApiConstant.WATCH_CONFIG, Header.EMPTY, body, WatchResponse.class, receiver);
    }
}
