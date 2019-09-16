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
package com.lessspring.org.configuration.schedule;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
public class Schedule {

    private static final int MIN_POO_SIE = 2;
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 20;
    private static final long KEEP_ALIVE_TIME = 10;
    private static final TimeUnit UNIT = TimeUnit.SECONDS;

    private static ThreadFactory Publisher_Thread_Factory = new ThreadFactory() {
        private final AtomicInteger nextId = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            String namePrefix = "Publisher_THREAD-" + nextId.getAndDecrement();
            return new Thread(r, namePrefix);
        }
    };

    private static ThreadFactory MQ_Thread_Factory = new ThreadFactory() {
        private final AtomicInteger nextId = new AtomicInteger(1);

        @Override
        public Thread newThread(@NotNull Runnable r) {
            String namePrefix = "Config_Operation_Mq_THREAD-";
            String name = namePrefix + nextId.getAndDecrement();
            return new Thread(r, name);
        }
    };

    public static final ExecutorService PUBLISHER = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
            Publisher_Thread_Factory);

    public static final ThreadPoolExecutor MQ = new ThreadPoolExecutor(MIN_POO_SIE, MIN_POO_SIE, KEEP_ALIVE_TIME, UNIT, new LinkedBlockingQueue<>(), MQ_Thread_Factory);

}
