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
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component
public class ConfigOperationService {

    private final Disruptor<ConfigChangeEvent> disruptorHolder;
    private final PresistenceHandler presistenceHandler;

    public ConfigOperationService(PresistenceHandler presistenceHandler, ConfigPersistenceHandler configPersistenceHandler) {
        this.presistenceHandler = presistenceHandler;
        Disruptor<ConfigChangeEvent> disruptor = DisruptorFactory.build(ConfigChangeEvent::new, "Config-Change-Event-Disruptor");
        disruptor.handleEventsWith(configPersistenceHandler);
        disruptorHolder = disruptor;
    }

    public ResponseData queryConfig(QueryConfigRequest request) {
        return ResponseData.success(presistenceHandler.readConfigContent(request));
    }

    public ResponseData publishConfig(PublishConfigRequest request) {
        if (presistenceHandler.saveConfigInfo(request)) {
            ConfigChangeEvent event = buildConfigChangeEvent(request, request.getContent(), EventType.PUBLISH);
            publishEvent(event);
            return ResponseData.success();
        }
        return ResponseData.fail();
    }

    public ResponseData modifyConfig(PublishConfigRequest request) {
        if (presistenceHandler.modifyConfigInfo(request)) {
            ConfigChangeEvent event = buildConfigChangeEvent(request, request.getContent(), EventType.MODIFIED);
            publishEvent(event);
            return ResponseData.success();
        }
        return ResponseData.fail();
    }

    public ResponseData removeConfig(DeleteConfigRequest request) {
        if (presistenceHandler.removeConfigInfo(request)) {
            ConfigChangeEvent event = buildConfigChangeEvent(request, "", EventType.DELETE);
            publishEvent(event);
            return ResponseData.success();
        }
        return ResponseData.fail();
    }

    private void publishEvent(ConfigChangeEvent source) {
        disruptorHolder.publishEvent((target, sequence) -> ConfigChangeEvent.copy(sequence, source, target));
    }

    private ConfigChangeEvent buildConfigChangeEvent(BaseConfigRequest request, String content, EventType type) {
        return ConfigChangeEvent.builder()
                .namespaceId(request.getNamespaceId())
                .dataId(request.getDataId())
                .groupId(request.getGroupId())
                .content(content)
                .source(this)
                .eventType(type)
                .build();
    }
}
