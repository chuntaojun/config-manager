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

import com.lessspring.org.auth.AuthHolder;
import com.lessspring.org.auth.LoginHandler;
import com.lessspring.org.cluster.ClusterChoose;
import com.lessspring.org.cluster.ClusterNodeWatch;
import com.lessspring.org.config.ConfigService;
import com.lessspring.org.http.HttpClient;
import com.lessspring.org.http.impl.ConfigHttpClient;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.model.vo.PublishConfigRequest;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.watch.WatchConfigWorker;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class ClientConfigService implements ConfigService {

    private HttpClient httpClient;
    private WatchConfigWorker watchConfigWorker;
    private ClusterNodeWatch clusterNodeWatch;
    private CacheConfigManager configManager;
    private LoginHandler loginHandler;
    private final AuthHolder authHolder = new AuthHolder();
    private final Configuration configuration;

    private final AtomicBoolean inited = new AtomicBoolean(false);
    private final AtomicBoolean destroyed = new AtomicBoolean(false);

    public ClientConfigService(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void init() {
        if (inited.compareAndSet(false, true)) {
            PathUtils.init(configuration.getCachePath());
            // Build a cluster node selector
            ClusterChoose choose = new ClusterChoose();

            httpClient = new ConfigHttpClient(choose, authHolder);
            loginHandler = new LoginHandler(httpClient, authHolder, configuration);
            clusterNodeWatch = new ClusterNodeWatch(httpClient, configuration);
            clusterNodeWatch.register(choose);

            choose.setWatch(clusterNodeWatch);

            watchConfigWorker = new WatchConfigWorker(httpClient, configuration);
            configManager = new CacheConfigManager(httpClient, configuration, watchConfigWorker);

            // The calling component all initialization of the hook
            httpClient.init();
            clusterNodeWatch.init();
            loginHandler.init();
            watchConfigWorker.init();
            configManager.init();
            watchConfigWorker.setConfigManager(configManager);
        }
    }

    @Override
    public void destroy() {
        if (destroyed.compareAndSet(false, true)) {
            httpClient.destroy();
            loginHandler.destroy();
            clusterNodeWatch.destroy();
            watchConfigWorker.destroy();
            configManager.destroy();
        }
    }

    @Override
    public ConfigInfo getConfig(String groupId, String dataId) {
        return getConfig(groupId, dataId, "");
    }

    @Override
    public ConfigInfo getConfig(String groupId, String dataId, String encryption) {
        return configManager.query(groupId, dataId, encryption);
    }

    @Override
    public boolean publishConfig(String groupId, String dataId, String content, String type) {
        return publishConfig(groupId, dataId, content, type, "");
    }

    @Override
    public boolean publishConfig(String groupId, String dataId, String content, String type, String encryption) {
        final PublishConfigRequest request = PublishConfigRequest.builder()
                .groupId(groupId)
                .dataId(dataId)
                .content(content)
                .encryption(encryption)
                .type(type)
                .build();
        ResponseData<Boolean> response = configManager.publishConfig(request);
        return response.ok();
    }

    @Override
    public boolean publishConfigFile(String groupId, String dataId, byte[] stream) {
        final PublishConfigRequest request = PublishConfigRequest.builder()
                .groupId(groupId)
                .dataId(dataId)
                .file(stream)
                .build();
        ResponseData<Boolean> response = configManager.publishConfig(request);
        return response.ok();
    }

    @Override
    public boolean deleteConfig(String groupId, String dataId) {
        return configManager.removeConfig(groupId, dataId).ok();
    }

    @Override
    public void addListener(String groupId, String dataId, AbstractListener... listeners) {
        addListener(groupId, dataId, "", listeners);
    }

    @Override
    public void addListener(String groupId, String dataId, String encryption, AbstractListener... listeners) {
        for (AbstractListener listener : listeners) {
            watchConfigWorker.registerListener(groupId, dataId, encryption, listener);
        }
    }

    @Override
    public void removeListener(String groupId, String dataId, AbstractListener... listeners) {
        for (AbstractListener listener : listeners) {
            watchConfigWorker.deregisterListener(groupId, dataId, listener);
        }
    }

}
