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

import com.lessspring.org.NameUtils;
import com.lessspring.org.model.vo.DeleteConfigRequest;
import com.lessspring.org.model.vo.PublishConfigRequest;
import com.lessspring.org.model.vo.QueryConfigRequest;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.pojo.event.BaseEvent;
import com.lessspring.org.pojo.event.ConfigChangeEvent;
import com.lessspring.org.event.EventType;
import com.lessspring.org.service.publish.ConfigPersistenceHandler;
import com.lessspring.org.utils.DiskUtils;
import com.lessspring.org.utils.DisruptorFactory;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Component
public class ConfigOperationService {

    private final Disruptor<? extends BaseEvent> disruptorHolder;

    public ConfigOperationService(ConfigPersistenceHandler persistenceHandler) {
        Disruptor<ConfigChangeEvent> disruptor = DisruptorFactory.build(ConfigChangeEvent.class);
        disruptor.handleEventsWith(persistenceHandler);
        disruptorHolder = disruptor;
    }

    public ResponseData queryConfig(QueryConfigRequest request) {
        final String path = request.getNamespaceId();
        final String fileName = NameUtils.buildName(request.getGroupId(), request.getDataId());
        String content = DiskUtils.readFile(path, fileName);
        return ResponseData.builder()
                .withCode(200)
                .withData(content)
                .build();
    }

    public ResponseData publishConfig(PublishConfigRequest request) {
        ConfigChangeEvent event = ConfigChangeEvent.builder()
                .namespaceId(request.getNamespaceId())
                .dataId(request.getDataId())
                .groupId(request.getGroupId())
                .content(request.getContent())
                .source(this)
                .eventType(EventType.PUBLISH)
                .build();
        publishEvent(event);
        return ResponseData.success();
    }

    public ResponseData modifyConfig(PublishConfigRequest request) {
        ConfigChangeEvent event = ConfigChangeEvent.builder()
                .namespaceId(request.getNamespaceId())
                .dataId(request.getDataId())
                .groupId(request.getGroupId())
                .content(request.getContent())
                .source(this)
                .eventType(EventType.MODIFIED)
                .build();
        publishEvent(event);
        return ResponseData.success();
    }

    public ResponseData removeConfig(DeleteConfigRequest request) {
        ConfigChangeEvent event = ConfigChangeEvent.builder()
                .namespaceId(request.getNamespaceId())
                .dataId(request.getDataId())
                .groupId(request.getGroupId())
                .source(this)
                .eventType(EventType.DELETE)
                .build();
        publishEvent(event);
        return ResponseData.success();
    }

    private void publishEvent(ConfigChangeEvent event) {
        disruptorHolder.publishEvent((event1, sequence) -> {});
    }
}
