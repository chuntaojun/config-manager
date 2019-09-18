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
package com.lessspring.org.http.impl;

import com.lessspring.org.model.vo.ResponseData;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public abstract class EventReceiver<T> {

    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("");
        return thread;
    });

    private BlockingQueue<ResponseData<T>> deferQueue = new LinkedBlockingQueue<>();

    public EventReceiver() {
        executor.submit((Runnable) () -> {
            while (true) {
                ResponseData<T> data = deferQueue.poll();
                onReceive(data);
            }
        });
    }

    void deferEvent(ResponseData<T> data) {
        try {
            deferQueue.put(data);
        } catch (InterruptedException ignore) {
        }
    }

    /**
     * Data receiving callback function
     *
     * @param data {@link ResponseData}
     */
    abstract void onReceive(ResponseData<T> data);

    /**
     * When the error occurs when the callback function
     *
     * @param throwable {@link Throwable}
     */
    abstract void onError(Throwable throwable);

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        executor.shutdown();
    }
}
