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
package com.lessspring.org.cluster;

import java.util.Iterator;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.lessspring.org.LifeCycle;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class ClusterChoose implements Observer, LifeCycle {

	private ClusterNodeWatch watch;

	private Iterator<String> clusterFind;

	private Set<String> clusterInfos;

	private String lastClusterIp;

	private final AtomicBoolean initialize = new AtomicBoolean(false);

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
	private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

	public ClusterChoose() {
	}

	public void setWatch(ClusterNodeWatch watch) {
		this.watch = watch;
	}

	@Override
	public void init() {
		if (initialize.compareAndSet(false, true)) {
			watch.register(this);
			clusterInfos = watch.copyNodeList();
			clusterFind = clusterInfos.iterator();
		}
	}

	@Override
	public void destroy() {
	}

	@Override
	public boolean isInited() {
		return false;
	}

	@Override
	public boolean isDestroyed() {
		return false;
	}

	public String getLastClusterIp() {
		if (StringUtils.isEmpty(lastClusterIp)) {
			refreshClusterIp();
		}
		return lastClusterIp;
	}

	public void refreshClusterIp() {
		readLock.lock();
		try {
			if (Objects.isNull(clusterInfos)) {
				init();
			}
			if (Objects.isNull(clusterFind) || !clusterFind.hasNext()) {
				clusterFind = clusterInfos.iterator();
			}
			lastClusterIp = clusterFind.next();
		}
		finally {
			readLock.unlock();
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		Set<String> newClusterInfo = (Set<String>) arg;
		writeLock.lock();
		try {
			clusterInfos = newClusterInfo;
			clusterFind = clusterInfos.iterator();
		}
		finally {
			writeLock.unlock();
		}
	}
}
