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

import com.google.gson.reflect.TypeToken;
import com.lessspring.org.NameUtils;
import com.lessspring.org.model.vo.BaseConfigRequest;
import com.lessspring.org.model.vo.DeleteConfigRequest;
import com.lessspring.org.model.vo.PublishConfigRequest;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.pojo.event.ConfigChangeEvent;
import com.lessspring.org.event.EventType;
import com.lessspring.org.pojo.request.PublishConfigRequest4;
import com.lessspring.org.pojo.request.QueryConfigRequest4;
import com.lessspring.org.raft.OperationEnum;
import com.lessspring.org.raft.Transaction;
import com.lessspring.org.raft.dto.Datum;
import com.lessspring.org.service.cluster.ClusterManager;
import com.lessspring.org.service.cluster.FailCallback;
import com.lessspring.org.service.distributed.ConfigTransactionCommitCallback;
import com.lessspring.org.utils.DisruptorFactory;
import com.lessspring.org.utils.GsonUtils;
import com.lessspring.org.utils.PropertiesEnum;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Service
public class ConfigOperationService {

    private final Disruptor<ConfigChangeEvent> disruptorHolder;
    private final PersistentHandler persistentHandler;
    private final ConfigTransactionCommitCallback commitCallback;
    private final ClusterManager clusterManager;
    private FailCallback failCallback;

    private final Type type = new TypeToken<Map<String, Object>>(){}.getType();


    public ConfigOperationService(@Qualifier(value = "encryptionPersistentHandler") PersistentHandler persistentHandler,
                                  ConfigPersistenceHandler configPersistenceHandler,
                                  ConfigTransactionCommitCallback commitCallback,
                                  ClusterManager clusterManager) {
        this.persistentHandler = persistentHandler;
        this.commitCallback = commitCallback;
        this.clusterManager = clusterManager;
        disruptorHolder = DisruptorFactory.build(ConfigChangeEvent::new, "Config-Change-Event-Disruptor");
        disruptorHolder.handleEventsWithWorkerPool(configPersistenceHandler);
        disruptorHolder.start();
    }

    @PostConstruct
    public void init() {
        commitCallback.registerConsumer(publishConsumer(), OperationEnum.PUBLISH);
        commitCallback.registerConsumer(modifyConsumer(), OperationEnum.MODIFY);
        commitCallback.registerConsumer(deleteConsumer(), OperationEnum.DELETE);
        failCallback = throwable -> null;
    }

    @PreDestroy
    public void shutdown() {
        disruptorHolder.shutdown();
    }

    public ResponseData<?> queryConfig(String namespaceId, QueryConfigRequest4 request) {
        return ResponseData.success(persistentHandler.readConfigContent(namespaceId, request));
    }

    public ResponseData<?> publishConfig(String namespaceId, PublishConfigRequest4 request) {
        Map<String, Object> attribute = new HashMap<>(2);
        attribute.put("namespaceId", namespaceId);
        attribute.put("publishConfigRequest", request);
        String key = NameUtils.buildName(PropertiesEnum.InterestKey.CONFIG_DARA.getType(), namespaceId, request.getGroupId(), request.getDataId());
        Datum datum = new Datum(key, GsonUtils.toJsonBytes(attribute), Map.class.getCanonicalName());
        datum.setOperationEnum(OperationEnum.PUBLISH);
        CompletableFuture<ResponseData<Boolean>> future = clusterManager.commit(datum, failCallback);
        try {
            return future.get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseData.fail();
        }
    }

    public ResponseData<?> modifyConfig(String namespaceId, PublishConfigRequest4 request) {
        Map<String, Object> attribute = new HashMap<>(2);
        attribute.put("namespaceId", namespaceId);
        attribute.put("publishConfigRequest", request);
        String key = NameUtils.buildName(PropertiesEnum.InterestKey.CONFIG_DARA.getType(), namespaceId, request.getGroupId(), request.getDataId());
        Datum datum = new Datum(key, GsonUtils.toJsonBytes(attribute), Map.class.getCanonicalName());
        datum.setOperationEnum(OperationEnum.MODIFY);
        CompletableFuture<ResponseData<Boolean>> future = clusterManager.commit(datum, failCallback);
        try {
            return future.get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseData.fail();
        }
    }

    public ResponseData<?> removeConfig(String namespaceId, DeleteConfigRequest request) {
        Map<String, Object> attribute = new HashMap<>(2);
        attribute.put("namespaceId", namespaceId);
        attribute.put("deleteConfigRequest", request);
        String key = NameUtils.buildName(PropertiesEnum.InterestKey.CONFIG_DARA.getType(), namespaceId, request.getGroupId(), request.getDataId());
        Datum datum = new Datum(key, GsonUtils.toJsonBytes(attribute), Map.class.getCanonicalName());
        datum.setOperationEnum(OperationEnum.DELETE);
        CompletableFuture<ResponseData<Boolean>> future = clusterManager.commit(datum, failCallback);
        try {
            return future.get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseData.fail();
        }
    }

    private void publishEvent(ConfigChangeEvent source) {
        disruptorHolder.publishEvent((target, sequence) -> ConfigChangeEvent.copy(sequence, source, target));
    }

    private ConfigChangeEvent buildConfigChangeEvent(String namespaceId, BaseConfigRequest request, String content, String encryption, EventType type) {
        return ConfigChangeEvent.builder()
                .namespaceId(namespaceId)
                .dataId(request.getDataId())
                .groupId(request.getGroupId())
                .content(content)
                .encryption(encryption)
                .source(this)
                .eventType(type)
                .build();
    }

    private Consumer<Transaction> publishConsumer() {
        return transaction -> {
            Map<String, Object> attribute = GsonUtils.toObj(transaction.getData(), type);
            String namespaceId = (String) attribute.get("namespaceId");
            PublishConfigRequest request = (PublishConfigRequest) attribute.get("publishConfigRequest");
            if (persistentHandler.saveConfigInfo(namespaceId, request)) {
                ConfigChangeEvent event = buildConfigChangeEvent(namespaceId, request, request.getContent(), request.getEncryption(), EventType.PUBLISH);
                event.setConfigType(request.getType());
                publishEvent(event);
            }
        };
    }

    private Consumer<Transaction> modifyConsumer() {
        return transaction -> {
            Map<String, Object> attribute = GsonUtils.toObj(transaction.getData(), type);
            String namespaceId = (String) attribute.get("namespaceId");
            PublishConfigRequest request = (PublishConfigRequest) attribute.get("publishConfigRequest");
            if (persistentHandler.modifyConfigInfo(namespaceId, request)) {
                ConfigChangeEvent event = buildConfigChangeEvent(namespaceId, request, request.getContent(), request.getEncryption(), EventType.MODIFIED);
                event.setConfigType(request.getType());
                publishEvent(event);
            }
        };
    }

    private Consumer<Transaction> deleteConsumer() {
        return transaction -> {
            Map<String, Object> attribute = GsonUtils.toObj(transaction.getData(), type);
            String namespaceId = (String) attribute.get("namespaceId");
            DeleteConfigRequest request = (DeleteConfigRequest) attribute.get("deleteConfigRequest");
            if (persistentHandler.removeConfigInfo(namespaceId, request)) {
                ConfigChangeEvent event = buildConfigChangeEvent(namespaceId, request, "", "", EventType.DELETE);
                publishEvent(event);
            }
        };
    }

}
