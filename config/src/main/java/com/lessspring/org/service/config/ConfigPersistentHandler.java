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
import com.lessspring.org.service.publish.WatchClientManager;
import com.lessspring.org.utils.ByteUtils;
import com.lessspring.org.utils.ConfigRequestUtils;
import com.lessspring.org.utils.DisruptorFactory;
import com.lessspring.org.utils.PropertiesEnum;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component(value = "persistentHandler")
public class ConfigPersistentHandler implements PersistentHandler, WorkHandler<ConfigChangeEvent> {

    private final Disruptor<NotifyEvent> disruptorHolder;

    private final ConfigCacheItemManager configCacheItemManager;

    @Resource
    private ConfigInfoMapper configInfoMapper;

    public ConfigPersistentHandler(WatchClientManager watchClientManager,
                                   ConfigCacheItemManager configCacheItemManager) {
        this.configCacheItemManager = configCacheItemManager;
        disruptorHolder = DisruptorFactory.build(NotifyEvent::new, "Notify-Event-Disruptor");
        disruptorHolder.handleEventsWithWorkerPool(watchClientManager);
        disruptorHolder.start();
    }

    @PreDestroy
    public void shutdown() {
        disruptorHolder.shutdown();
    }

    @Override
    public ConfigInfo readConfigContent(String namespaceId, BaseConfigRequest request) {
        final String dataId = request.getDataId();
        final String groupId = request.getGroupId();
        QueryConfigInfo queryConfigInfo = QueryConfigInfo.builder()
                .namespaceId(namespaceId)
                .groupId(groupId)
                .dataId(dataId)
                .build();
        ConfigInfoDTO dto = configInfoMapper.findConfigInfo(queryConfigInfo);
        byte[] origin = dto.getContent();
        // unable transport config-context encryption token
        ConfigInfo info = ConfigInfo.builder()
                .groupId(dto.getGroupId())
                .dataId(dto.getDataId())
                .type(dto.getType())
                .build();
        byte type = ByteUtils.getByteByIndex(origin, 0);
        byte[] source = ByteUtils.cut(origin, 1, origin.length - 1);
        if (Objects.equals(type, PropertiesEnum.ConfigType.FILE.getType())) {
            info.setFile(source);
        } else {
            info.setContent(new String(source, Charset.forName(StandardCharsets.UTF_8.name())));
        }
        return info;
    }

    @Override
    public boolean saveConfigInfo(String namespaceId, PublishConfigRequest request) {
        boolean success = true;
        int affect = -1;
        long id = -1;
        byte[] save = ConfigRequestUtils.getByte(request);
        try {
            if (request.isBeta()) {
                ConfigBetaInfoDTO infoDTO = ConfigBetaInfoDTO.builder()
                        .namespaceId(namespaceId)
                        .groupId(request.getGroupId())
                        .dataId(request.getDataId())
                        .content(save)
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
                        .content(save)
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
        byte[] save = ConfigRequestUtils.getByte(request);
        try {
            if (request.isBeta()) {
                ConfigBetaInfoDTO infoDTO = ConfigBetaInfoDTO.builder()
                        .namespaceId(namespaceId)
                        .groupId(request.getGroupId())
                        .dataId(request.getDataId())
                        .content(save)
                        .type(request.getType())
                        .clientIps(request.getClientIps())
                        .lastModifyTime(System.currentTimeMillis())
                        .build();
                affect = configInfoMapper.updateConfigBetaInfo(infoDTO);
            } else {
                ConfigInfoDTO infoDTO = ConfigInfoDTO.builder()
                        .namespaceId(namespaceId)
                        .groupId(request.getGroupId())
                        .dataId(request.getDataId())
                        .content(save)
                        .type(request.getType())
                        .lastModifyTime(System.currentTimeMillis())
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
            configCacheItemManager.updateContent(event.getNamespaceId(), event);
            if (EventType.PUBLISH.compareTo(event.getEventType()) == 0){
                configCacheItemManager.registerConfigCacheItem(event.getNamespaceId(), event);
            }
            if (EventType.DELETE.compareTo(event.getEventType()) == 0) {
                configCacheItemManager.deregisterConfigCacheItem(event.getNamespaceId(), event);
                return;
            }
            NotifyEvent source = NotifyEvent.builder()
                    .namespaceId(event.getNamespaceId())
                    .groupId(event.getGroupId())
                    .dataId(event.getDataId())
                    .eventType(event.getEventType())
                    .entryption(event.getEncryption())
                    .build();
            disruptorHolder.publishEvent((target, sequence1) -> NotifyEvent.copy(sequence1, source, target));
        } catch (Exception e) {
            log.error("notify ConfigChangeEvent has some error : {}", e);
        }
    }
}
