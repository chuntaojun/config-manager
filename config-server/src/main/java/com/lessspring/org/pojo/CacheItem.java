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
package com.lessspring.org.pojo;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.lessspring.org.NameUtils;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class CacheItem {

	public static final short CACHE_ITEM = 0;
	public static final short TEMP_CACHE_ITEM = 0;

	private final String namespaceId;

	private final String groupId;

	private final String dataId;

	private final boolean file;

	private volatile String lastMd5;

	private volatile long lastUpdateTime;

	private volatile boolean beta;

	private final String key;

	private Set<String> betaClientIps = new CopyOnWriteArraySet<>();

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
	private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

	public CacheItem(String namespaceId, String groupId, String dataId, boolean file) {
		this.namespaceId = namespaceId;
		this.groupId = groupId;
		this.dataId = dataId;
		this.file = file;
		this.key = NameUtils.buildName(namespaceId, groupId, dataId);
	}

	public String getNamespaceId() {
		return namespaceId;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getDataId() {
		return dataId;
	}

	public String getLastMd5() {
		return lastMd5;
	}

	public void setLastMd5(String lastMd5) {
		this.lastMd5 = lastMd5;
	}

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public boolean isBeta() {
		return beta;
	}

	public void setBeta(boolean beta) {
		this.beta = beta;
	}

	public Set<String> getBetaClientIps() {
		return betaClientIps;
	}

	public void setBetaClientIps(Set<String> betaClientIps) {
		for (String clientIp : betaClientIps) {
			if (Objects.equals("0.0.0.0:0", clientIp)) {
				this.betaClientIps = Collections.emptySet();
				return;
			}
		}
		this.betaClientIps = betaClientIps;
	}

	public String getKey() {
		return key;
	}

	public boolean canRead(String clientIp) {
		return betaClientIps.isEmpty() || !betaClientIps.contains(clientIp);
	}

	public void executeReadWork(ReadWork readWork) {
		try {
			if (readLock.tryLock(1000, TimeUnit.MILLISECONDS)) {
				try {
					readWork.job();
				}
				catch (Exception e) {
					readWork.onError(e);
				}
				finally {
					readLock.unlock();
				}
			}
		}
		catch (InterruptedException ignore) {

		}
	}

	public void executeWriteWork(WriteWork writeWork) {
		try {
			if (writeLock.tryLock(1000, TimeUnit.MILLISECONDS)) {
				try {
					writeWork.job();
				}
				catch (Exception e) {
					writeWork.onError(e);
				}
				finally {
					writeLock.unlock();
				}
			}
		}
		catch (InterruptedException ignore) {

		}
	}
}
