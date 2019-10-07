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

import com.lessspring.org.service.dump.task.DumpTask4All;
import com.lessspring.org.service.dump.task.DumpTask4Beta;
import com.lessspring.org.service.dump.task.DumpTask4Period;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Component(value = "dumpService")
public class DumpService {

    private final DumpProcessor<DumpTask4All> task4AllDumpProcessor;
    private final DumpProcessor<DumpTask4Beta> task4BetaDumpProcessor;
    private final DumpProcessor<DumpTask4Period> task4PeriodDumpProcessor;

    private final ScheduledThreadPoolExecutor executor;

    public DumpService() {
        this.task4AllDumpProcessor = new DumpAllProcessor();
        this.task4BetaDumpProcessor = new DumpAllBetaProcessor();
        this.task4PeriodDumpProcessor = new DumpPeriodProcessor();

        this.executor = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("com.lessspring.org.service.dump.Executor");
            return thread;
        });
    }

    @PostConstruct
    public void init() {
        Long[] ids = new Long[]{0L};
        dumpAll(ids).run();
        dumpAllBeta(ids).run();
        executor.scheduleAtFixedRate(dumpPeriod(), 10_000L, 15 * 60 * 1000L, TimeUnit.MILLISECONDS);
    }

    private Runnable dumpAll(Long[] ids) {
        return () -> {
            int batchSize = 1_000;
            int counter = 0;
            List<Long> batchWork = new ArrayList<>(batchSize);
            for (long id : ids) {
                batchWork.add(id);
                counter ++;
                if (counter > batchSize) {
                    counter = 0;
                    DumpTask4All task4All = new DumpTask4All(batchWork.toArray(new Long[0]));
                    task4AllDumpProcessor.process(task4All);
                    batchWork.clear();
                }
            }
        };
    }

    private Runnable dumpAllBeta(Long[] ids) {
        return () -> {
            int batchSize = 1_000;
            int counter = 0;
            List<Long> batchWork = new ArrayList<>(batchSize);
            for (long id : ids) {
                batchWork.add(id);
                counter ++;
                if (counter > batchSize) {
                    counter = 0;
                    DumpTask4Beta task4Beta = new DumpTask4Beta(batchWork.toArray(new Long[0]));
                    task4BetaDumpProcessor.process(task4Beta);
                    batchWork.clear();
                }
            }
        };
    }

    private Runnable dumpPeriod() {
        return () -> {
        };
    }
}
