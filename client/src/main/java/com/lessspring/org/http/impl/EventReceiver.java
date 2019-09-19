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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.lessspring.org.model.vo.ResponseData;
import okhttp3.Call;

import java.util.Objects;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public abstract class EventReceiver<T> {

    private static final EventBus DEFER_PUBLISHER = new EventBus("config-watch-event-publisher");

    private Call call;

    public EventReceiver() {
        DEFER_PUBLISHER.register(this);
    }

    void deferEvent(ResponseData<T> data) {
        DEFER_PUBLISHER.post(data);
    }

    /**
     * Data receiving callback function
     *
     * @param data {@link ResponseData}
     */
    @Subscribe
    public abstract void onReceive(ResponseData<T> data);

    /**
     * When the error occurs when the callback function
     *
     * @param throwable {@link Throwable}
     */
    public abstract void onError(Throwable throwable);

    void setCall(Call call) {
        this.call = call;
    }

    public void cancle() {
        DEFER_PUBLISHER.unregister(this);
        if (Objects.nonNull(call) && !call.isCanceled()) {
            call.cancel();
        }
    }
}
