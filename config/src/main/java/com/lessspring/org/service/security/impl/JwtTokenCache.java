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
package com.lessspring.org.service.security.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.time.Duration;
import java.util.Objects;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class JwtTokenCache {

    private final Cache<String, Long> tokenCache;

    public JwtTokenCache() {
        this.tokenCache = CacheBuilder.newBuilder()
                .maximumSize(65535)
                .expireAfterWrite(Duration.ofSeconds(30))
                .build();
    }

    public boolean isExist(String key) {
        return Objects.nonNull(tokenCache.getIfPresent(key));
    }

    public void addToken(String key, Long value) {
        tokenCache.put(key, value);
    }

    public void removeToken(String key) {
        tokenCache.invalidate(key);
    }

}
