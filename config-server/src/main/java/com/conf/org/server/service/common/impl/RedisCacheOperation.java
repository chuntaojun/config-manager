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
package com.conf.org.server.service.common.impl;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.conf.org.utils.GsonUtils;
import com.conf.org.server.service.common.CacheOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class RedisCacheOperation implements CacheOperation {

	@Autowired
	private ReactiveRedisTemplate<String, String> redisTemplate;

	@Override
	public Optional<byte[]> get(String key) {
		CompletableFuture<byte[]> future = new CompletableFuture<>();
		redisTemplate.opsForValue().get(key).subscribe(o -> {
			Entry entry = GsonUtils.toObj(String.valueOf(o), Entry.class);
			future.complete(
					entry.data.getBytes(Charset.forName(StandardCharsets.UTF_8.name())));
		});
		try {
			return Optional.of(future.get(1000, TimeUnit.MILLISECONDS));
		}
		catch (InterruptedException | ExecutionException | TimeoutException e) {
			return Optional.empty();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> getObj(String key) {
		CompletableFuture<T> future = new CompletableFuture<>();
		redisTemplate.opsForValue().get(key).subscribe(o -> {
			Entry entry = GsonUtils.toObj(String.valueOf(o), Entry.class);
			try {
				T t = (T) GsonUtils.toObj(entry.data, Class.forName(entry.className));
				future.complete(t);
			}
			catch (ClassNotFoundException e) {
				future.complete(null);
			}
		});
		try {
			return Optional.of(future.get(1000, TimeUnit.MILLISECONDS));
		}
		catch (InterruptedException | ExecutionException | TimeoutException e) {
			return Optional.empty();
		}
	}

	@Override
	public void put(String key, byte[] value) {
		Entry entry = new Entry();
		entry.data = new String(value, Charset.forName(StandardCharsets.UTF_8.name()));
		redisTemplate.opsForValue().set(key, GsonUtils.toJson(entry));
	}

	@Override
	public <T> void put(String key, T t) {
		String className = t.getClass().getCanonicalName();
		Entry entry = new Entry();
		entry.data = GsonUtils.toJson(t);
		entry.className = className;
		redisTemplate.opsForValue().set(key, GsonUtils.toJson(entry));
	}

	@Override
	public void put(String key, byte[] value, long liveTime) {
		Entry entry = new Entry();
		entry.data = new String(value, Charset.forName(StandardCharsets.UTF_8.name()));
		redisTemplate.opsForValue().set(key, GsonUtils.toJson(entry));
		redisTemplate.expire(key, Duration.ofMillis(liveTime));
	}

	@Override
	public <T> void put(String key, T t, long liveTime) {
		String className = t.getClass().getCanonicalName();
		Entry entry = new Entry();
		entry.data = GsonUtils.toJson(t);
		entry.className = className;
		redisTemplate.opsForValue().set(key, GsonUtils.toJson(entry));
		redisTemplate.expire(key, Duration.ofMillis(liveTime));
	}

	@Override
	public void expire(String key) {
		redisTemplate.expireAt(key, Instant.now());
	}

	private static class Entry {

		protected String data;
		protected String className;

	}

}
