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
package com.lessspring.org.service.metrics;

import com.lessspring.org.LifeCycle;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class Metrics implements LifeCycle {

    private static final Metrics INSTANCE = new Metrics();

    static {
        INSTANCE.init();
    }

    public static Metrics getInstance() {
        return INSTANCE;
    }

    private Gauge monitor;
    private Histogram clientRequestHistogram;

    private Metrics() {}

    @Override
    public void init() {
        this.monitor = Gauge.build()
                .name("config_manager_monitor").labelNames("module", "name")
                .help("config_manager_monitor").register();
        this.clientRequestHistogram = Histogram.build().labelNames("module", "method", "url", "code")
                .name("config_manager_client_request").help("config_manager_client_request")
                .register();
    }

    @Override
    public void destroy() {

    }
}
