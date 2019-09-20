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

import com.lessspring.org.api.ApiConstant;
import com.lessspring.org.http.HttpClient;
import com.lessspring.org.http.param.Body;
import com.lessspring.org.http.param.Header;
import com.lessspring.org.http.param.Query;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.model.vo.PublishConfigRequest;
import com.lessspring.org.model.vo.QueryConfigRequest;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.utils.GsonUtils;
import com.lessspring.org.watch.WatchConfigWorker;

import java.util.Objects;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class CacheConfigManager implements LifeCycle {

    private WatchConfigWorker worker;

    private HttpClient httpClient;

    private final String namespaceId;

    CacheConfigManager(HttpClient client, Configuration configuration, WatchConfigWorker worker) {
        this.httpClient = client;
        this.worker = worker;
        this.namespaceId = configuration.getNamespaceId();
    }

    @Override
    public void init() {
        this.worker.setConfigManager(this);
    }

    public ConfigInfo query(String groupId, String dataId) {
        final QueryConfigRequest request = QueryConfigRequest.builder()
                .groupId(groupId)
                .dataId(dataId)
                .build();
        final Query query = Query.newInstance()
                .addParam("namespaceId", namespaceId)
                .addParam("query", request);
        ResponseData<ConfigInfo> response = httpClient.get(ApiConstant.QUERY_CONFIG, Header.EMPTY, query, ConfigInfo.class);
        if (response.isOk()) {
            ConfigInfo configInfo = response.getData();
            doSnapshot(groupId, dataId, configInfo);
            return configInfo;
        }
        ConfigInfo local = loadFromDisk(groupId, dataId);
        if (Objects.nonNull(local)) {
            return local;
        }
        return null;
    }

    public boolean removeConfig(String groupId, String dataId) {
        return false;
    }

    public ResponseData<Boolean> publishConfig(final PublishConfigRequest request) {
        final Query query = Query.newInstance()
                .addParam("namespaceId", namespaceId);
        return httpClient.put(ApiConstant.PUBLISH_CONFIG, Header.EMPTY, query, Body.objToBody(request), Boolean.class);
    }

    private ConfigInfo loadFromDisk(String groupId, String dataId) {
        final String fileName = NameUtils.buildName("snapshot", groupId, dataId);
        final String content = DiskUtils.readFile(namespaceId, fileName);
        return GsonUtils.toObj(content, ConfigInfo.class);
    }

    private void doSnapshot(String groupId, String dataId, ConfigInfo configInfo) {
        final String fileName = NameUtils.buildName("snapshot", groupId, dataId);
        DiskUtils.writeFile(namespaceId, fileName, GsonUtils.toJsonBytes(configInfo));
    }

    @Override
    public void destroy() {
        httpClient.destroy();
        worker.destroy();
        worker = null;
        httpClient = null;
    }
}
