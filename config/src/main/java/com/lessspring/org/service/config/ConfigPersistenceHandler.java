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
import com.lessspring.org.db.dto.ConfigBetaInfoDTO;
import com.lessspring.org.db.dto.ConfigInfoDTO;
import com.lessspring.org.event.EventType;
import com.lessspring.org.model.vo.BaseConfigRequest;
import com.lessspring.org.model.vo.DeleteConfigRequest;
import com.lessspring.org.model.vo.PublishConfigRequest;
import com.lessspring.org.pojo.event.ConfigChangeEvent;
import com.lessspring.org.pojo.event.NotifyEvent;
import com.lessspring.org.pojo.query.QueryConfigInfo;
import com.lessspring.org.repository.ConfigInfoMapper;
import com.lessspring.org.utils.DiskUtils;
import com.lessspring.org.utils.DisruptorFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component(value = "presistenceHandler")
public class ConfigPersistenceHandler implements PresistenceHandler, EventHandler<ConfigChangeEvent> {

    private final Disruptor<NotifyEvent> disruptorHolder;

    @Resource
    private ConfigInfoMapper configInfoMapper;

    public ConfigPersistenceHandler() {
        disruptorHolder = DisruptorFactory.build(NotifyEvent::new, "Notify-Event-Disruptor");
    }

    @Override
    public String readConfigContent(BaseConfigRequest request) {
        final String namespaceId = request.getNamespaceId();
        final String dataId = request.getDataId();
        final String groupId = request.getGroupId();
        QueryConfigInfo queryConfigInfo = QueryConfigInfo.builder()
                .namespaceId(namespaceId)
                .groupId(groupId)
                .dataId(dataId)
                .build();
        return configInfoMapper.findConfigInfo(queryConfigInfo).getContent();
    }

    @Override
    public boolean saveConfigInfo(PublishConfigRequest request) {
        boolean success = true;
        try {
            if (request.isBeta()) {
                ConfigBetaInfoDTO infoDTO = ConfigBetaInfoDTO.builder()
                        .namespaceId(request.getNamespaceId())
                        .groupId(request.getGroupId())
                        .dataId(request.getDataId())
                        .content(request.getContent())
                        .type(request.getType())
                        .clientIps(request.getClientIps())
                        .createTime(System.currentTimeMillis())
                        .build();
                int affect = configInfoMapper.saveConfigBetaInfo(infoDTO);
            } else {
                ConfigInfoDTO infoDTO = ConfigInfoDTO.builder()
                        .namespaceId(request.getNamespaceId())
                        .groupId(request.getGroupId())
                        .dataId(request.getDataId())
                        .content(request.getContent())
                        .type(request.getType())
                        .createTime(System.currentTimeMillis())
                        .build();
                int affect = configInfoMapper.saveConfigInfo(infoDTO);
                long id = infoDTO.getId();
            }
        } catch (Exception e) {
            success = false;
            log.error("save config-info failed, err is : {}", e.getMessage());
        }
        return success;
    }

    @Override
    public boolean modifyConfigInfo(PublishConfigRequest request) {
        boolean success = true;
        try {
            if (request.isBeta()) {
                ConfigBetaInfoDTO infoDTO = ConfigBetaInfoDTO.builder()
                        .namespaceId(request.getNamespaceId())
                        .groupId(request.getGroupId())
                        .dataId(request.getDataId())
                        .content(request.getContent())
                        .type(request.getType())
                        .clientIps(request.getClientIps())
                        .createTime(System.currentTimeMillis())
                        .build();
                int affect = configInfoMapper.updateConfigBetaInfo(infoDTO);
            } else {
                ConfigInfoDTO infoDTO = ConfigInfoDTO.builder()
                        .namespaceId(request.getNamespaceId())
                        .groupId(request.getGroupId())
                        .dataId(request.getDataId())
                        .content(request.getContent())
                        .type(request.getType())
                        .createTime(System.currentTimeMillis())
                        .build();
                int affect = configInfoMapper.updateConfigInfo(infoDTO);
                long id = infoDTO.getId();
            }
        } catch (Exception e) {
            success = false;
            log.error("modify config-info failed, err is : {}", e.getMessage());
        }
        return success;
    }

    @Override
    public boolean removeConfigInfo(DeleteConfigRequest request) {
        boolean success = true;
        try {
            if (request.isBeta()) {
                configInfoMapper.removeConfigBetaInfo(request);
            } else {
                configInfoMapper.removeConfigInfo(request);
            }
        } catch (Exception e) {
            success = false;
            log.error("remove config-info failed, err is : {}", e.getMessage());
        }
        return success;
    }

    @Override
    public void onEvent(ConfigChangeEvent event, long sequence, boolean endOfBatch) throws Exception {
        final String namespaceId = event.getNamespaceId();
        final String groupId = event.getGroupId();
        final String dataId = event.getDataId();
        if (event.getEventType() == EventType.DELETE) {
            DiskUtils.deleteFile(namespaceId, NameUtils.buildName(groupId, dataId));
            return;
        }
        DiskUtils.writeFile(namespaceId, NameUtils.buildName(groupId, dataId), event.getContent().getBytes(StandardCharsets.UTF_8));
        NotifyEvent source = NotifyEvent.builder()
                .namespaceId(event.getNamespaceId())
                .groupId(event.getGroupId())
                .dataId(event.getDataId())
                .eventType(event.getEventType())
                .build();
        disruptorHolder.publishEvent((target, sequence1) -> NotifyEvent.copy(sequence1, target, source));
    }
}
