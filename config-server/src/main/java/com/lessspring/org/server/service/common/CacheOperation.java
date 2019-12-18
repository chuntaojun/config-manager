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
package com.lessspring.org.server.service.common;

import java.util.Optional;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public interface CacheOperation {

	/**
	 * get value by key
	 *
	 * @param key key
	 * @return {@link Optional<byte[]>}
	 */
	Optional<byte[]> get(String key);

	/**
	 * get value by key
	 *
	 * @param key key
	 * @param <T> type
	 * @return {@link Optional<T>}
	 */
	<T> Optional<T> getObj(String key);

	/**
	 * put key-value
	 *
	 * @param key key
	 * @param value value
	 */
	void put(String key, byte[] value);

	/**
	 * put key-value
	 *
	 * @param key key
	 * @param t value
	 */
	<T> void put(String key, T t);

	/**
	 * put key-value with life-Time
	 *
	 * @param key key
	 * @param value value
	 * @param liveTime liveTime
	 */
	void put(String key, byte[] value, long liveTime);

	/**
	 * put key-value with life-Time
	 *
	 * @param key key
	 * @param t value
	 * @param liveTime liveTime
	 */
	<T> void put(String key, T t, long liveTime);

	/**
	 * expire this key
	 *
	 * @param key key
	 */
	void expire(String key);

}
