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
import com.lessspring.org.model.vo.BaseConfigRequest;
import com.lessspring.org.model.vo.DeleteConfigRequest;
import com.lessspring.org.model.vo.PublishConfigRequest;
import com.lessspring.org.service.publish.EventStaging;
import com.lessspring.org.DiskUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Primary
@Component(value = "cachePresistenceHandler")
public class CacheableConfigPresistenceHandler implements PresistenceHandler {

    private final PresistenceHandler presistenceHandler;
    private final EventStaging eventStaging;

    public CacheableConfigPresistenceHandler(
            @Qualifier(value = "presistenceHandler") PresistenceHandler presistenceHandler,
            EventStaging eventStaging) {
        this.presistenceHandler = presistenceHandler;
        this.eventStaging = eventStaging;
    }

    @Override
    public String readConfigContent(String namespaceId, BaseConfigRequest request) {
        final String dataId = request.getDataId();
        final String groupId = request.getGroupId();
        String content = DiskUtils.readFile(namespaceId, NameUtils.buildName(groupId, dataId));
        if (StringUtils.isEmpty(content)) {
            return presistenceHandler.readConfigContent(namespaceId, request);
        }
        return content;
    }

    @Override
    public boolean saveConfigInfo(String namespaceId, PublishConfigRequest request) {
        String key = NameUtils.buildName(namespaceId, request.getGroupId(), request.getDataId());
        eventStaging.invalidate(key);
        return presistenceHandler.saveConfigInfo(namespaceId, request);
    }

    @Override
    public boolean modifyConfigInfo(String namespaceId, PublishConfigRequest request) {
        String key = NameUtils.buildName(namespaceId, request.getGroupId(), request.getDataId());
        eventStaging.invalidate(key);
        return presistenceHandler.modifyConfigInfo(namespaceId, request);
    }

    @Override
    public boolean removeConfigInfo(String namespaceId, DeleteConfigRequest request) {
        String key = NameUtils.buildName(namespaceId, request.getGroupId(), request.getDataId());
        eventStaging.invalidate(key);
        return presistenceHandler.removeConfigInfo(namespaceId, request);
    }

}
