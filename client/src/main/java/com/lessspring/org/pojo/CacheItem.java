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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import com.lessspring.org.AbstractListener;
import com.lessspring.org.model.dto.ConfigInfo;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class CacheItem {

	private String groupId;
	private String dataId;
	private String lastMd5 = "";
	private ConfigInfo oldInfo;
	private CopyOnWriteArrayList<AbstractListener> listeners;

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getLastMd5() {
		return lastMd5;
	}

	public void setLastMd5(String lastMd5) {
		this.lastMd5 = lastMd5;
	}

	public void setConfigInfo(ConfigInfo info) {
		this.oldInfo = info;
	}

	public ConfigInfo getConfigInfo() {
		return oldInfo;
	}

	public void addListener(AbstractListener listener) {
		if (listeners == null) {
			synchronized (this) {
				if (listeners == null) {
					listeners = new CopyOnWriteArrayList<>();
				}
			}
		}
		listeners.add(listener);
	}

	public void removeListener(AbstractListener listener) {
		if (Objects.isNull(listeners)) {
			throw new IllegalStateException("listener list is null");
		}
		listeners.remove(listener);
	}

	public List<AbstractListener> listListener() {
		return listeners;
	}

	public boolean isChange(String remoteMd5) {
		return !Objects.equals(lastMd5, remoteMd5);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CacheItem cacheItem = (CacheItem) o;
		return groupId.equals(cacheItem.groupId) && dataId.equals(cacheItem.dataId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(groupId, dataId);
	}

	public static CacheItemBuilder builder() {
		return new CacheItemBuilder();
	}

	public static final class CacheItemBuilder {
		private String groupId;
		private String dataId;
		private String lastMd5 = "";

		private CacheItemBuilder() {
		}

		public CacheItemBuilder withGroupId(String groupId) {
			this.groupId = groupId;
			return this;
		}

		public CacheItemBuilder withDataId(String dataId) {
			this.dataId = dataId;
			return this;
		}

		public CacheItemBuilder withLastMd5(String lastMd5) {
			this.lastMd5 = lastMd5;
			return this;
		}

		public CacheItem build() {
			CacheItem cacheItem = new CacheItem();
			cacheItem.setGroupId(groupId);
			cacheItem.setDataId(dataId);
			cacheItem.setLastMd5(lastMd5);
			return cacheItem;
		}
	}
}
