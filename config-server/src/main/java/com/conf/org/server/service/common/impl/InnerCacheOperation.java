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

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.conf.org.executor.NameThreadFactory;
import com.conf.org.server.service.common.CacheOperation;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@SuppressWarnings("all")
public class InnerCacheOperation implements CacheOperation {

	private final InnerCache<String, Object> cache = new InnerCache<String, Object>();
	private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
			1, new NameThreadFactory("com.lessspring.org.cache.clean-Worker"));

	@PostConstruct
	public void init() {
		executor.scheduleAtFixedRate(cache.newCleanExpireWork(), 60, 120,
				TimeUnit.SECONDS);
	}

	@PreDestroy
	public void destroy() {
		executor.shutdown();
	}

	@Override
	public Optional<byte[]> get(String key) {
		return Optional.ofNullable((byte[]) cache.get(key));
	}

	@Override
	public <T> Optional<T> getObj(String key) {
		return Optional.ofNullable((T) cache.get(key));
	}

	@Override
	public void put(String key, byte[] value) {
		cache.put(key, value);
	}

	@Override
	public <T> void put(String key, T t) {
		cache.put(key, t);
	}

	@Override
	public void put(String key, byte[] value, long liveTime) {
		cache.put(key, value, liveTime);
	}

	@Override
	public <T> void put(String key, T t, long liveTime) {
		cache.put(key, t, liveTime);
	}

	@Override
	public void expire(String key) {
		cache.remove(key);
	}

	private static class InnerCache<K, V> {

		private final Map<K, Entry<V>> cache = new ConcurrentHashMap<>(64);
		private final Map<K, Long> expireRecord = new ConcurrentHashMap<>(64);

		public V get(String key) {
			Entry<V> entry = cache.get(key);
			if (entry == null) {
				return null;
			}
			if (entry instanceof ExpireEntry) {
				ExpireEntry<V> vExpireEntry = (ExpireEntry<V>) entry;
				if (System.currentTimeMillis() > vExpireEntry.expireTime) {
					return null;
				}
			}
			return entry.data;
		}

		public void put(K k, V v) {
			Entry<V> entry = new Entry<>();
			entry.data = v;
			cache.put(k, entry);
		}

		public void put(K k, V v, Long lifeTime) {
			ExpireEntry<V> entry = new ExpireEntry<>();
			entry.data = v;
			entry.expireTime = System.currentTimeMillis() + lifeTime;
			cache.put(k, entry);
			expireRecord.put(k, entry.expireTime);
		}

		public void remove(K k) {
			cache.remove(k);
		}

		Runnable newCleanExpireWork() {
			return new CleanExpireWork();
		}

		class CleanExpireWork implements Runnable {

			@Override
			public void run() {
				Iterator<Map.Entry<K, Long>> iterator = expireRecord.entrySet()
						.iterator();
				while (iterator.hasNext()) {
					Map.Entry<K, Long> record = iterator.next();
					if (System.currentTimeMillis() > record.getValue()) {
						iterator.remove();
						remove(record.getKey());
					}
				}
			}
		}

	}

	private static class Entry<T> {

		protected T data;

	}

	private static class ExpireEntry<T> extends Entry {

		private long expireTime;

	}
}
