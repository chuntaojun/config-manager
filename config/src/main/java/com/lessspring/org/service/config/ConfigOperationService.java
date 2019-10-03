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

import com.lessspring.org.model.vo.BaseConfigRequest;
import com.lessspring.org.model.vo.DeleteConfigRequest;
import com.lessspring.org.model.vo.PublishConfigRequest;
import com.lessspring.org.model.vo.QueryConfigRequest;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.pojo.event.ConfigChangeEvent;
import com.lessspring.org.event.EventType;
import com.lessspring.org.utils.DisruptorFactory;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PreDestroy;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Service
public class ConfigOperationService {

    private final Disruptor<ConfigChangeEvent> disruptorHolder;
    private final PersistentHandler persistentHandler;

    public ConfigOperationService(@Qualifier(value = "encryptionPersistentHandler") PersistentHandler persistentHandler,
                                  ConfigPersistenceHandler configPersistenceHandler) {
        this.persistentHandler = persistentHandler;
        disruptorHolder = DisruptorFactory.build(ConfigChangeEvent::new, "Config-Change-Event-Disruptor");
        disruptorHolder.handleEventsWithWorkerPool(configPersistenceHandler);
        disruptorHolder.start();
    }

    @PreDestroy
    public void shutdown() {
        disruptorHolder.shutdown();
    }

    public ResponseData<?> queryConfig(String namespaceId, QueryConfigRequest request) {
        return ResponseData.success(persistentHandler.readConfigContent(namespaceId, request));
    }

    public ResponseData<?> publishConfig(String namespaceId, PublishConfigRequest request) {
        if (persistentHandler.saveConfigInfo(namespaceId, request)) {
            ConfigChangeEvent event = buildConfigChangeEvent(namespaceId, request, request.getContent(), "", EventType.PUBLISH);
            event.setConfigType(request.getType());
            publishEvent(event);
            return ResponseData.success();
        }
        return ResponseData.fail();
    }

    public ResponseData<?> modifyConfig(String namespaceId, PublishConfigRequest request) {
        if (persistentHandler.modifyConfigInfo(namespaceId, request)) {
            ConfigChangeEvent event = buildConfigChangeEvent(namespaceId, request, request.getContent(), "", EventType.MODIFIED);
            event.setConfigType(request.getType());
            publishEvent(event);
            return ResponseData.success();
        }
        return ResponseData.fail();
    }

    public ResponseData<?> removeConfig(String namespaceId, DeleteConfigRequest request) {
        if (persistentHandler.removeConfigInfo(namespaceId, request)) {
            ConfigChangeEvent event = buildConfigChangeEvent(namespaceId, request, "", "", EventType.DELETE);
            publishEvent(event);
            return ResponseData.success();
        }
        return ResponseData.fail();
    }

    private void publishEvent(ConfigChangeEvent source) {
        disruptorHolder.publishEvent((target, sequence) -> ConfigChangeEvent.copy(sequence, source, target));
    }

    private ConfigChangeEvent buildConfigChangeEvent(String namespaceId, BaseConfigRequest request, String content, String entryption, EventType type) {
        return ConfigChangeEvent.builder()
                .namespaceId(namespaceId)
                .dataId(request.getDataId())
                .groupId(request.getGroupId())
                .content(content)
                .entryption(entryption)
                .source(this)
                .eventType(type)
                .build();
    }
}
