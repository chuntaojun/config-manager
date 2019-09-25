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
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.model.vo.BaseConfigRequest;
import com.lessspring.org.model.vo.DeleteConfigRequest;
import com.lessspring.org.model.vo.PublishConfigRequest;
import com.lessspring.org.pojo.event.ConfigChangeEvent;
import com.lessspring.org.pojo.event.NotifyEvent;
import com.lessspring.org.pojo.query.QueryConfigInfo;
import com.lessspring.org.repository.ConfigInfoMapper;
import com.lessspring.org.DiskUtils;
import com.lessspring.org.service.publish.WatchClientManager;
import com.lessspring.org.utils.DisruptorFactory;
import com.lessspring.org.utils.GsonUtils;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component(value = "presistenceHandler")
public class ConfigPersistenceHandler implements PresistenceHandler, WorkHandler<ConfigChangeEvent> {

    private final Disruptor<NotifyEvent> disruptorHolder;

    @Resource
    private ConfigInfoMapper configInfoMapper;

    public ConfigPersistenceHandler(WatchClientManager watchClientManager) {
        disruptorHolder = DisruptorFactory.build(NotifyEvent::new, "Notify-Event-Disruptor");
        disruptorHolder.handleEventsWithWorkerPool(watchClientManager);
        disruptorHolder.start();
    }

    @PreDestroy
    public void shutdown() {
        disruptorHolder.shutdown();
    }

    @Override
    public String readConfigContent(String namespaceId, BaseConfigRequest request) {
        final String dataId = request.getDataId();
        final String groupId = request.getGroupId();
        QueryConfigInfo queryConfigInfo = QueryConfigInfo.builder()
                .namespaceId(namespaceId)
                .groupId(groupId)
                .dataId(dataId)
                .build();
        return new String(configInfoMapper.findConfigInfo(queryConfigInfo).getContent(), Charset.forName(StandardCharsets.UTF_8.name()));
    }

    @Override
    public boolean saveConfigInfo(String namespaceId, PublishConfigRequest request) {
        boolean success = true;
        int affect = -1;
        long id = -1;
        try {
            if (request.isBeta()) {
                ConfigBetaInfoDTO infoDTO = ConfigBetaInfoDTO.builder()
                        .namespaceId(namespaceId)
                        .groupId(request.getGroupId())
                        .dataId(request.getDataId())
                        .content(request.getContent().getBytes(StandardCharsets.UTF_8))
                        .type(request.getType())
                        .clientIps(request.getClientIps())
                        .createTime(System.currentTimeMillis())
                        .build();
                affect = configInfoMapper.saveConfigBetaInfo(infoDTO);
                id = infoDTO.getId();
            } else {
                ConfigInfoDTO infoDTO = ConfigInfoDTO.builder()
                        .namespaceId(namespaceId)
                        .groupId(request.getGroupId())
                        .dataId(request.getDataId())
                        .content(request.getContent().getBytes(StandardCharsets.UTF_8))
                        .type(request.getType())
                        .createTime(System.currentTimeMillis())
                        .build();
                affect = configInfoMapper.saveConfigInfo(infoDTO);
                id = infoDTO.getId();
            }
            log.debug("save config-success, affect rows is : {}, primary key is : {}", affect, id);
        } catch (Exception e) {
            success = false;
            log.error("save config-info failed, err is : {}", e.getLocalizedMessage());
        }
        return success;
    }

    @Override
    public boolean modifyConfigInfo(String namespaceId, PublishConfigRequest request) {
        boolean success = true;
        int affect = -1;
        try {
            if (request.isBeta()) {
                ConfigBetaInfoDTO infoDTO = ConfigBetaInfoDTO.builder()
                        .namespaceId(namespaceId)
                        .groupId(request.getGroupId())
                        .dataId(request.getDataId())
                        .content(request.getContent().getBytes(StandardCharsets.UTF_8))
                        .type(request.getType())
                        .clientIps(request.getClientIps())
                        .createTime(System.currentTimeMillis())
                        .build();
                affect = configInfoMapper.updateConfigBetaInfo(infoDTO);
            } else {
                ConfigInfoDTO infoDTO = ConfigInfoDTO.builder()
                        .namespaceId(namespaceId)
                        .groupId(request.getGroupId())
                        .dataId(request.getDataId())
                        .content(request.getContent().getBytes(StandardCharsets.UTF_8))
                        .type(request.getType())
                        .createTime(System.currentTimeMillis())
                        .build();
                affect = configInfoMapper.updateConfigInfo(infoDTO);
            }
            log.debug("save config-success, affect rows is : {}", affect);
        } catch (Exception e) {
            success = false;
            log.error("modify config-info failed, err is : {}", e.getMessage());
        }
        return success;
    }

    @Override
    public boolean removeConfigInfo(String namespaceId, DeleteConfigRequest request) {
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
    public void onEvent(ConfigChangeEvent event) throws Exception {
        try {
            final String namespaceId = event.getNamespaceId();
            final String groupId = event.getGroupId();
            final String dataId = event.getDataId();
            if (event.getEventType() == EventType.DELETE) {
                DiskUtils.deleteFile(namespaceId, NameUtils.buildName(groupId, dataId));
                return;
            }
            final ConfigInfo configInfo = new ConfigInfo(groupId, dataId, event.getContent(), event.getConfigType());
            DiskUtils.writeFile(namespaceId, NameUtils.buildName(groupId, dataId), GsonUtils.toJsonBytes(configInfo));
            NotifyEvent source = NotifyEvent.builder()
                    .namespaceId(event.getNamespaceId())
                    .groupId(event.getGroupId())
                    .dataId(event.getDataId())
                    .eventType(event.getEventType())
                    .build();
            disruptorHolder.publishEvent((target, sequence1) -> NotifyEvent.copy(sequence1, source, target));
        } catch (Exception e) {
            log.error("notify ConfigChangeEvent has some error : {}", e);
        }
    }
}
