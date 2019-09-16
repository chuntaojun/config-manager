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
package com.lessspring.org.service.publish;

import com.lessspring.org.model.vo.BaseConfigRequest;
import com.lessspring.org.pojo.event.ConfigChangeEvent;
import com.lessspring.org.pojo.event.NotifyEvent;
import com.lessspring.org.utils.DisruptorFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Component
public class ConfigPersistenceHandler implements EventHandler<ConfigChangeEvent> {

    private final WatchClientManager watchClientManager;

    private final Disruptor<NotifyEvent> disruptorHolder;

    public ConfigPersistenceHandler(WatchClientManager watchClientManager) {
        this.watchClientManager = watchClientManager;
        disruptorHolder = DisruptorFactory.build(NotifyEvent.class);
    }

    public String readConfigContent(BaseConfigRequest request) {
        final String namespaceId = request.getNamespaceId();
        final String dataId = request.getDataId();
        final String groupId = request.getGroupId();
    }

    @Override
    public void onEvent(ConfigChangeEvent event, long sequence, boolean endOfBatch) throws Exception {

    }
}
