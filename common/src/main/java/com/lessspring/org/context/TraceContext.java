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

package com.lessspring.org.context;

import com.lessspring.org.IDUtils;
import com.lessspring.org.RequireHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * trace context
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019-11-28 16:27
 */
public final class TraceContext {

    private final Map<String, Object> attachments = new HashMap<>();

    private String traceId = "TraceId-" + IDUtils.generateUuid();

    public TraceContext() {
    }

    public synchronized void setAttachment(String key, Object value) {
        RequireHelper.requireNotNull(value, "value must not null");
        attachments.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T getAttachment(String key) {
        T t = (T) attachments.get(key);
        return t;
    }

    public String getTraceId() {
        return traceId;
    }

    @Override
    public String toString() {
        return "TraceContext{" +
                "traceId='" + traceId + '\'' +
                ", attachments=" + attachments +
                '}';
    }

    public void clean() {
        traceId = "TraceId-" + IDUtils.generateUuid();
        attachments.clear();
    }

}
