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

import com.lessspring.org.cluster.ClusterChoose;
import com.lessspring.org.cluster.ClusterNodeWatch;
import com.lessspring.org.config.ConfigService;
import com.lessspring.org.http.HttpClient;
import com.lessspring.org.http.impl.ConfigHttpClient;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.model.vo.PublishConfigRequest;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.watch.WatchConfigWorker;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class ClientConfigService implements ConfigService {

    private HttpClient httpClient;
    private WatchConfigWorker watchConfigWorker;
    private ClusterNodeWatch clusterNodeWatch;
    private CacheConfigManager configManager;
    private final Configuration configuration;

    public ClientConfigService(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void init() {
        PathUtils.init(configuration.getCachePath());
        // Build a cluster node selector
        ClusterChoose choose = new ClusterChoose();

        httpClient = new ConfigHttpClient(choose);
        ClusterNodeWatch clusterNodeWatch = new ClusterNodeWatch(httpClient, configuration);

        choose.setWatch(clusterNodeWatch);

        WatchConfigWorker watchConfigWorker = new WatchConfigWorker(httpClient, configuration);
        configManager = new CacheConfigManager(httpClient, configuration, watchConfigWorker);

        httpClient.init();
        clusterNodeWatch.init();
        watchConfigWorker.init();
        configManager.init();
        watchConfigWorker.setConfigManager(configManager);
    }

    @Override
    public void destroy() {
        httpClient.destroy();
        clusterNodeWatch.destroy();
        watchConfigWorker.destroy();
        configManager.destroy();
    }

    @Override
    public ConfigInfo getConfig(String groupId, String dataId) {
        return configManager.query(groupId, dataId);
    }

    @Override
    public boolean publishConfig(String groupId, String dataId, String content, String type) {
        final PublishConfigRequest request = PublishConfigRequest.builder()
                .groupId(groupId)
                .dataId(dataId)
                .content(content)
                .type(type)
                .build();
        ResponseData<Boolean> response = configManager.publishConfig(request);
        return response.isOk();
    }

    @Override
    public void addListener(String groupId, String dataId, AbstractListener... listeners) {
        for (AbstractListener listener : listeners) {
            watchConfigWorker.registerListener(groupId, dataId, listener);
        }
    }

    @Override
    public void removeListener(String groupId, String dataId, AbstractListener... listeners) {
        for (AbstractListener listener : listeners) {
            watchConfigWorker.registerListener(groupId, dataId, listener);
        }
    }

}
