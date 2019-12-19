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
package com.lessspring.org.common.limit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import com.google.common.util.concurrent.RateLimiter;
import com.lessspring.org.Configuration;
import com.lessspring.org.LifeCycle;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class RequestLimitManager implements LifeCycle {

	private Map<String, RateLimiter> limiterMap;

	private final AtomicBoolean inited = new AtomicBoolean(false);
	private final AtomicBoolean destroyed = new AtomicBoolean(false);

	private final Configuration configuration;

	public RequestLimitManager(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void init() {
		if (inited.compareAndSet(false, true)) {
			limiterMap = new ConcurrentHashMap<>(16);
		}
	}

	public boolean canSendRequest(String key) {
		limiterMap.computeIfAbsent(key, s -> RateLimiter.create(1000.0D));
		RateLimiter target = limiterMap.get(key);
		return target.tryAcquire();
	}

	public void canSendRequest(String key, Runnable runnable) {
		limiterMap.computeIfAbsent(key, s -> RateLimiter.create(1000.0D));
		RateLimiter target = limiterMap.get(key);
		if (target.tryAcquire()) {
			runnable.run();
		}
	}

	@Override
	public void destroy() {
		if (isInited() && destroyed.compareAndSet(false, true)) {
			limiterMap.clear();
		}
	}

	@Override
	public boolean isInited() {
		return inited.get();
	}

	@Override
	public boolean isDestroyed() {
		return destroyed.get();
	}
}
