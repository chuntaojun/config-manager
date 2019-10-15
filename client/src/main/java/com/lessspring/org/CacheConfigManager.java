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

import com.google.gson.reflect.TypeToken;
import com.lessspring.org.api.ApiConstant;
import com.lessspring.org.http.HttpClient;
import com.lessspring.org.http.param.Body;
import com.lessspring.org.http.param.Header;
import com.lessspring.org.http.param.Query;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.model.vo.PublishConfigRequest;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.utils.GsonUtils;
import com.lessspring.org.utils.PlaceholderProcessor;
import com.lessspring.org.watch.WatchConfigWorker;

import java.util.Objects;
import java.util.Optional;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class CacheConfigManager implements LifeCycle {

    private WatchConfigWorker worker;

    private SerializerUtils serializer = SerializerUtils.getInstance();

    private HttpClient httpClient;

    private final String namespaceId;

    private final PlaceholderProcessor processor;

    CacheConfigManager(HttpClient client, Configuration configuration,
                       WatchConfigWorker worker) {
        this.httpClient = client;
        this.worker = worker;
        this.namespaceId = configuration.getNamespaceId();
        this.processor = new PlaceholderProcessor();
    }

    @Override
    public void init() {
        this.worker.setConfigManager(this);
    }

    ConfigInfo query(String groupId, String dataId, String token) {
        final Query query = Query.newInstance()
                .addParam("namespaceId", namespaceId)
                .addParam("groupId", groupId)
                .addParam("dataId", dataId);
        ResponseData<ConfigInfo> response = httpClient.get(ApiConstant.QUERY_CONFIG,
                Header.EMPTY, query, new TypeToken<ResponseData<ConfigInfo>>(){});
        ConfigInfo result = null;
        if (response.ok()) {
            ConfigInfo configInfo = response.getData();
            snapshotSave(groupId, dataId, configInfo);
            result = configInfo;
        } else {
            // Disaster measures
            ConfigInfo local = snapshotLoad(groupId, dataId);
            if (Objects.nonNull(local)) {
                result = local;
            }
        }
        // Configure the decryption
        processor.decryption(Optional.ofNullable(result), token);
        return result;
    }

    ResponseData<Boolean> removeConfig(String groupId, String dataId) {
        final Query query = Query.newInstance()
                .addParam("namespaceId", namespaceId)
                .addParam("groupId", groupId)
                .addParam("dataId", dataId);
        return httpClient.delete(ApiConstant.DELETE_CONFIG, Header.EMPTY,
                query, new TypeToken<ResponseData<Boolean>>(){});
    }

    ResponseData<Boolean> publishConfig(final PublishConfigRequest request) {
        final Query query = Query.newInstance()
                .addParam("namespaceId", namespaceId);
        return httpClient.put(ApiConstant.PUBLISH_CONFIG, Header.EMPTY, query,
                Body.objToBody(request), new TypeToken<ResponseData<Boolean>>(){});
    }

    private ConfigInfo snapshotLoad(String groupId, String dataId) {
        final String fileName = NameUtils.buildName("snapshot", groupId, dataId);
        final byte[] content = DiskUtils.readFileBytes(namespaceId, fileName);
        if (Objects.nonNull(content) && content.length > 0) {
            return serializer.deserialize(content, ConfigInfo.class);
        }
        return null;
    }

    private void snapshotSave(String groupId, String dataId, ConfigInfo configInfo) {
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
