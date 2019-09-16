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
package com.lessspring.org.utils;

import com.lessspring.org.configuration.schedule.Schedule;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class DisruptorFactory {

    public static <T> Disruptor<T> build(Class<T> type) {
        EventFactory<T> factory = () -> {
            try {
                return type.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        };
        int ringBufferSize = 1024 * 1024;
        return new Disruptor<>(factory, ringBufferSize, new ThreadFactory() {
            private final AtomicInteger nextId = new AtomicInteger(1);

            @Override
            public Thread newThread(@NotNull Runnable r) {
                String namePrefix = "Config_Operation_Mq_THREAD-";
                String name = namePrefix + nextId.getAndDecrement();
                return new Thread(r, name);
            }
        }, ProducerType.SINGLE, new BlockingWaitStrategy());
    }

}
