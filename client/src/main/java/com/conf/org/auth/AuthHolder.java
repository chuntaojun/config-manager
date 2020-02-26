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
package com.conf.org.auth;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.conf.org.model.vo.JwtResponse;
import com.conf.org.observer.Publisher;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class AuthHolder extends Publisher<CountDownLatch> {

	private long threshold = TimeUnit.SECONDS.toMillis(3);
	private volatile long lastRefreshTime;
	private volatile long expireTime;
	private volatile String token = "";

	void register(LoginHandler handler) {
		registerWatcher(handler);
	}

	synchronized void updateToken(JwtResponse token) {
		this.token = Objects.isNull(token.getToken()) ? "" : token.getToken();
		lastRefreshTime = System.currentTimeMillis();
		expireTime = token.getExpireTime();
	}

	public String getToken() {
		try {
			if (expireTime - lastRefreshTime < threshold) {
				CountDownLatch latch = new CountDownLatch(1);
				notifyAllWatcher(latch);
				long waitTime = 10L;
				latch.await(waitTime, TimeUnit.SECONDS);
			}
			return token;
		}
		catch (InterruptedException ignore) {
			return token;
		}
	}

	public void refresh() {
		try {
			CountDownLatch latch = new CountDownLatch(1);
			notifyAllWatcher(latch);
			long waitTime = 10L;
			latch.await(waitTime, TimeUnit.SECONDS);
		}
		catch (Exception ignore) {
		}
	}

}
