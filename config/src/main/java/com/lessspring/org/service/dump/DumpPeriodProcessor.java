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
package com.lessspring.org.service.dump;

import com.lessspring.org.repository.ConfigInfoMapper;
import com.lessspring.org.service.dump.task.DumpTask4Period;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class DumpPeriodProcessor implements DumpProcessor<DumpTask4Period> {

    private final ConfigInfoMapper configInfoMapper;
    private final Consumer<Long[]> task4AllDumpProcessor;
    private final Consumer<Long[]> task4BetaDumpProcessor;
    private ScheduledThreadPoolExecutor executor;

    public DumpPeriodProcessor(ConfigInfoMapper configInfoMapper,
                               Consumer<Long[]> task4AllDumpProcessor,
                               Consumer<Long[]> task4BetaDumpProcessor) {
        this.configInfoMapper = configInfoMapper;
        this.task4AllDumpProcessor = task4AllDumpProcessor;
        this.task4BetaDumpProcessor = task4BetaDumpProcessor;

    }

    @Override
    public void process(DumpTask4Period dumpTask) {
        long seconds = dumpTask.getPeriod().getSeconds();
        // Task cycle
        executor.scheduleAtFixedRate(() -> {
            // Asynchronous dump mission operations
            Long[] ids = configInfoMapper.findMinAndMaxId().toArray(new Long[0]);
            task4AllDumpProcessor.accept(ids);
            Long[] ids4Beta = configInfoMapper.findMinAndMaxId4Beta().toArray(new Long[0]);
            task4BetaDumpProcessor.accept(ids4Beta);
        }, seconds / 2, seconds, TimeUnit.SECONDS);
    }

    @Override
    public void init() {
        this.executor = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("com.lessspring.org.service.dump.Executor");
            return thread;
        });
    }

    @Override
    public void destroy() {
        this.executor.shutdown();
    }
}
