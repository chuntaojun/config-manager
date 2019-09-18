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

import com.lessspring.org.cluster.ClusterNodeWatch;
import com.lessspring.org.config.ConfigService;
import com.lessspring.org.watch.WatchConfigWorker;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class ClientConfigService implements ConfigService {

    private WatchConfigWorker watchConfigWorker;
    private ClusterNodeWatch clusterNodeWatch;

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void create(Configuration configuration) {

    }

    @Override
    public String getConfig(String groupId, String dataId) {
        return null;
    }

    @Override
    public boolean publishConfig(String groupId, String dataId, String content, String type) {
        return false;
    }

    @Override
    public void addListener(String groupId, String dataId, AbstractListener... listeners) {

    }

}
