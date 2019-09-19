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
package com.lessspring.org.model.vo;

import com.lessspring.org.utils.GsonUtils;

import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class WatchRequest {

    public static final String QUERY_KEY = "watchParams";

    private String namespaceId = "default";
    private Map<String, String> watchKey = Collections.emptyMap();

    public WatchRequest(String namespaceId, Map<String, String> watchKey) {
        this.namespaceId = namespaceId;
        this.watchKey = watchKey;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public Map<String, String> getWatchKey() {
        return watchKey;
    }

    public void setWatchKey(Map<String, String> watchKey) {
        this.watchKey = watchKey;
    }

    @Override
    public String toString() {
        return GsonUtils.toJson(this);
    }
}
