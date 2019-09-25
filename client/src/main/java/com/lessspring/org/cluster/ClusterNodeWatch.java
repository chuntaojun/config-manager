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
package com.lessspring.org.cluster;

import com.google.common.eventbus.EventBus;
import com.lessspring.org.Configuration;
import com.lessspring.org.LifeCycle;
import com.lessspring.org.api.ApiConstant;
import com.lessspring.org.http.HttpClient;
import com.lessspring.org.http.Retry;
import com.lessspring.org.http.param.Header;
import com.lessspring.org.http.param.Query;
import com.lessspring.org.model.vo.ResponseData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.lessspring.org.api.Code.SUCCESS;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class ClusterNodeWatch implements LifeCycle {

    private ScheduledThreadPoolExecutor executor;

    private EventBus eventBus = new EventBus("cluster-watch-event");

    private final HttpClient httpClient;

    private Set<String> nodeList = new HashSet<>();

    public ClusterNodeWatch(HttpClient httpClient, Configuration configuration) {
        this.httpClient = httpClient;
        String[] clusterIps = configuration.getServers().split(",");
        nodeList.addAll(Arrays.asList(clusterIps));
    }

    @Override
    public void init() {

        executor = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("config-client-refresh-clusterInfo");
            return thread;
        });

        eventBus.post(nodeList);

        executor.schedule(this::refreshCluster, TimeUnit.SECONDS.toMillis(5), TimeUnit.MILLISECONDS);

    }

    @Override
    public void destroy() {
        executor.shutdown();
    }

    private void refreshCluster() {
        long delay = TimeUnit.SECONDS.toMillis(30);
        Retry<Boolean> retry = new Retry<Boolean>() {
            @Override
            protected Boolean run() throws Exception {
                ResponseData<Set> response = httpClient.get(ApiConstant.REFRESH_CLUSTER_NODE_INFO, Header.EMPTY, Query.EMPTY, Set.class);
                if (response.getCode() == SUCCESS.getCode()) {
                    Set<String> result = (Set<String>) response.getData();
                    ClusterNodeWatch.this.nodeList = result;
                    return true;
                }
                return false;
            }

            @Override
            protected boolean shouldRetry(Boolean data, Throwable throwable) {
                return true;
            }

            @Override
            protected int maxRetry() {
                return 3;
            }
        };

        boolean success = retry.work();
        if (!success) {
            delay = 0;
        }

        executor.schedule(this::refreshCluster, delay, TimeUnit.MILLISECONDS);
    }

    public void register(Object watcher) {
        eventBus.register(watcher);
    }

    public Set<String> copyNodeList() {
        return new HashSet<>(nodeList);
    }

}
