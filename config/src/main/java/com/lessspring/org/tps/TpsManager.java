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
package com.lessspring.org.tps;

import com.google.common.util.concurrent.RateLimiter;
import com.lessspring.org.constant.Code;
import com.lessspring.org.model.vo.ResponseData;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Component
public class TpsManager {

    private final Map<String, LimitRuleEntry> limiterManager = new HashMap<>(8);

    public LimitRuleEntry query(String key) {
        return limiterManager.get(key);
    }

    public Map<String, LimitRuleEntry> getLimiterManager() {
        return limiterManager;
    }

    public synchronized void registerLimiter(String key, Supplier<LimitRuleEntry> supplier) {
        limiterManager.computeIfAbsent(key, s ->  supplier.get());
    }

    public static class LimitRuleEntry {

        private LimitRule limitRule;
        private FailStrategy strategy;
        private RateLimiter rateLimiter;

        public LimitRule getLimitRule() {
            return limitRule;
        }

        public void setLimitRule(LimitRule limitRule) {
            this.limitRule = limitRule;
        }

        public void setStrategy(FailStrategy strategy) {
            this.strategy = strategy;
        }

        public void setRateLimiter(RateLimiter rateLimiter) {
            this.rateLimiter = rateLimiter;
        }

        public ResponseData<?> tryAcquire() {
            return rateLimiter.tryAcquire() ? null : (strategy == null ?
                    ResponseData.fail(Code.SERVER_BUSY) : strategy.onLimit());
        }
    }

}