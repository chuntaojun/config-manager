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

import com.lessspring.org.CasReadWriteLock;
import com.lessspring.org.NameUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
public class CacheItem {

	private final String namespaceId;

	private final String groupId;

	private final String dataId;

	private final boolean file;
	private final String key;

	/**
	 * 一个简单的读写锁实现
	 */
	private final CasReadWriteLock casReadWriteLock = new CasReadWriteLock();
	private volatile String lastMd5;
	private volatile long lastUpdateTime;
	private volatile boolean beta;
	/**
	 * 用于控制通知客户端时的新旧版本控制，避免就=旧版本覆盖新版本
	 */
	private volatile long version = 0;
	private Set<String> betaClientIps = new CopyOnWriteArraySet<>();

	public CacheItem(String namespaceId, String groupId, String dataId, boolean file,
			long version) {
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

	public synchronized void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public long getVersion() {
		return version;
	}

	/**
	 * 高危操作，谨慎调用
	 *
	 * @param version
	 */
	public void setVersion(long version) {
		this.version = version;
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
			// if beta-client-ip has contain "0.0.0.0"(IP) or "*"(ClientId)
			// beta will be lose efficacy
			if (Objects.equals("0.0.0.0:0", clientIp) || Objects.equals("*", clientIp)) {
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
		boolean a = betaClientIps.isEmpty();
		boolean b = betaClientIps.contains(clientIp);
		return a || b;
	}

	// 需要考虑清楚，如果写线程到了悲观锁，而读线程因为乐观锁而正在执行，
	// 那么存在读写同时运行的情况，需要考虑如何避免此类现象，确保线程安全
	// 返回值只代表任务是否被执行了

	public boolean executeReadWork(ReadWork readWork) {
		log.warn("execute read work ");
		if (casReadWriteLock.tryReadLock()) {
			try {
				readWork.job();
			}
			catch (Exception e) {
				readWork.onError(e);
			}
			finally {
				casReadWriteLock.unReadLock();
			}
			return true;
		}
		else {
			log.warn("");
			return false;
		}
	}

	public boolean executeWriteWork(WriteWork writeWork) {
		log.warn("execute write work ");
		if (casReadWriteLock.tryWriteLock()) {
			try {
				writeWork.job();
			}
			catch (Exception e) {
				writeWork.onError(e);
			}
			finally {
				casReadWriteLock.unWriteLock();
			}
			return true;
		}
		else {
			log.warn(
					"Failed to acquire write lock, no chance to execute, exit execution");
			return false;
		}
	}
}
