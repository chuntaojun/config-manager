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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

/**
 * trace context
 *
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019-11-28 16:27
 */
public final class TraceContext {

    private static final TraceContextHolder INSTANCE = TraceContextHolder.getInstance();

    private final Map<String, Object> attachments = new HashMap<>();

    private TraceContext parentTraceContext;
    private String traceId = "TraceId-" + IDUtils.generateUuid();
    private LinkedList<TraceContext> subContexts;

    public TraceContext() {
        this.parentTraceContext = null;
    }

    public TraceContext(TraceContext parentTraceContext) {
        this.parentTraceContext = parentTraceContext;
    }

    public synchronized void setAttachment(String key, Object value) {
        if (Objects.isNull(value)) {
            throw new IllegalArgumentException("value cant not be null");
        }
        attachments.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T getAttachment(String key) {
        T t = (T) attachments.get(key);
        if (t == null && parentTraceContext != null) {
            return parentTraceContext.getAttachment(key);
        }
        return t;
    }

    public String getTraceId() {
        return traceId;
    }

    public TraceContext getParentTraceContext() {
        return parentTraceContext;
    }

    public LinkedList<TraceContext> getSubContexts() {
        return subContexts;
    }

    @Override
    public String toString() {
        return "TraceContext{" +
                "traceId='" + traceId + '\'' +
                ", attachments=" + attachments +
                '}';
    }

    public void clean() {
        parentTraceContext = null;
        traceId = "TraceId-" + IDUtils.generateUuid();
        attachments.clear();
        if (Objects.nonNull(subContexts)) {
            subContexts.clear();
        }
    }

    public synchronized TraceContext createSubContext() {
        TraceContext sub = new TraceContext(this);
        INSTANCE.setInvokeTraceContext(sub);
        if (subContexts == null) {
            subContexts = new LinkedList<>();
        }
        subContexts.addLast(sub);
        return sub;
    }
}
