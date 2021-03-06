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
package com.lessspring.org;

import com.google.common.collect.ImmutableMap;
import com.google.gson.reflect.TypeToken;
import com.lessspring.org.api.ApiConstant;
import com.lessspring.org.constant.WatchType;
import com.lessspring.org.filter.ConfigFilterManager;
import com.lessspring.org.http.HttpClient;
import com.lessspring.org.http.param.Body;
import com.lessspring.org.http.param.Header;
import com.lessspring.org.http.param.Query;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.model.vo.PublishConfigRequest;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.server.pojo.CacheItem;
import com.lessspring.org.server.utils.GsonUtils;
import com.lessspring.org.server.utils.PathConstants;
import com.lessspring.org.watch.AbstractWatchWorker;
import com.lessspring.org.watch.ChangeKeyListener;
import com.lessspring.org.watch.WrapperListener;
import com.lessspring.org.watch.longpoll.LongPollWatchConfigWorker;
import com.lessspring.org.watch.sse.SseWatchConfigWorker;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Manager of all actively pulled and monitored configuration items
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class CacheConfigManager implements LifeCycle {

	private SerializerUtils serializer = SerializerUtils.getInstance();

	private ImmutableMap<String, CacheItem> itemImmutableMap;

	private HttpClient httpClient;
	private AbstractWatchWorker watchWorker;

	private final Map<String, CacheItem> cacheItemMap;
	private final String namespaceId;
	private final boolean localPref;
	private final ConfigFilterManager configFilterManager;
	private final AtomicBoolean inited = new AtomicBoolean(false);
	private final AtomicBoolean destroyed = new AtomicBoolean(false);

	CacheConfigManager(HttpClient client, Configuration configuration,
			ConfigFilterManager configFilterManager) {
		this.httpClient = client;
		this.namespaceId = configuration.getNamespaceId();
		this.configFilterManager = configFilterManager;
		this.localPref = configuration.isLocalPref();
		this.watchWorker = buildWatchWorker(httpClient, configuration,
				configFilterManager);
		this.cacheItemMap = new ConcurrentHashMap<>(16);
	}

	@Override
	public void init() {
		if (inited.compareAndSet(false, true)) {
			this.watchWorker.setConfigManager(this);
		}
	}

	private AbstractWatchWorker buildWatchWorker(HttpClient httpClient,
			Configuration configuration, ConfigFilterManager configFilterManager) {
		if (configuration.getWatchType() == WatchType.SSE) {
			return new SseWatchConfigWorker(httpClient, configuration,
					configFilterManager);
		}
		return new LongPollWatchConfigWorker(httpClient, configuration,
				configFilterManager);
	}

	void deregisterListener(String groupId, String dataId, AbstractListener listener) {
		CacheItem cacheItem = getCacheItem(groupId, dataId);
		cacheItem.removeListener(new WrapperListener(listener, ""));
	}

	private Tuple2<Boolean, CacheItem> computeIfAbsentCacheItem(String groupId,
			String dataId, String encryption) {
		final String key = NameUtils.buildName(groupId, dataId);
		final boolean[] add = new boolean[] { false };
		Supplier<CacheItem> supplier = () -> {
			add[0] = true;
			return CacheItem.builder().withGroupId(groupId).withDataId(dataId)
					.withLastMd5("").withToken(encryption).build();
		};
		cacheItemMap.computeIfAbsent(key, s -> supplier.get());
		if (add[0]) {
			// update cacheItemMap snapshot
			itemImmutableMap = null;
		}
		return Tuples.of(add[0], cacheItemMap.get(key));
	}

	public List<AbstractListener> allListener(String key) {
		final CacheItem item = cacheItemMap.get(key);
		return Objects.isNull(item) ? Collections.emptyList() : item.listListener();
	}

	public void registerListener(String groupId, String dataId, String encryption,
			AbstractListener listener) {
		Tuple2<Boolean, CacheItem> tuple2 = computeIfAbsentCacheItem(groupId, dataId,
				encryption);

		// if listener instance of ChangeKeyListener, should set CacheConfigManager into
		// Listener

		if (listener instanceof ChangeKeyListener) {
			ReflectUtils.inject(listener, this, "configManager");
		}
		tuple2.getT2().addListener(new WrapperListener(listener, encryption));
		if (tuple2.getT1()) {
			watchWorker.onChange();
		}
	}

	public Map<String, CacheItem> copy() {
		if (Objects.isNull(itemImmutableMap)) {
			synchronized (this) {
				itemImmutableMap = ImmutableMap.copyOf(cacheItemMap);
			}
		}
		return itemImmutableMap;
	}

	private void removeCacheItem(String groupId, String dataId) {
		String key = NameUtils.buildName(groupId, dataId);
		if (cacheItemMap.containsKey(key)) {
			cacheItemMap.remove(key);
			watchWorker.onChange();
		}
	}

	public CacheItem getCacheItem(String groupId, String dataId) {
		final String key = NameUtils.buildName(groupId, dataId);
		return cacheItemMap.get(key);
	}

	public ConfigInfo query(String groupId, String dataId, String token) {
		ConfigInfo result = null;
		if (localPref) {
			result = localPreference(groupId, dataId);
		}
		if (result == null) {
			final Query query = Query.newInstance().addParam("namespaceId", namespaceId)
					.addParam("groupId", groupId).addParam("dataId", dataId);
			ResponseData<ConfigInfo> response = httpClient.get(ApiConstant.QUERY_CONFIG,
					Header.EMPTY, query, new TypeToken<ResponseData<ConfigInfo>>() {
					});
			if (response.ok()) {
				ConfigInfo configInfo = response.getData();
				snapshotSave(groupId, dataId, configInfo);
				result = configInfo;
			}
			else {
				// Disaster measures
				ConfigInfo local = snapshotLoad(groupId, dataId);
				if (Objects.nonNull(local)) {
					result = local;
				}
			}
		}
		// Configure the decryption
		if (result != null) {
			result.setEncryption(token);
			configFilterManager.doFilter(result);
			result.setEncryption("");
		}
		return result;
	}

	ResponseData<Boolean> removeConfig(String groupId, String dataId) {
		final Query query = Query.newInstance().addParam("namespaceId", namespaceId)
				.addParam("groupId", groupId).addParam("dataId", dataId);
		return httpClient.delete(ApiConstant.DELETE_CONFIG, Header.EMPTY, query,
				new TypeToken<ResponseData<Boolean>>() {
				});
	}

	ResponseData<Boolean> publishConfig(final PublishConfigRequest request) {
		final Query query = Query.newInstance().addParam("namespaceId", namespaceId);
		return httpClient.put(ApiConstant.PUBLISH_CONFIG, Header.EMPTY, query,
				Body.objToBody(request), new TypeToken<ResponseData<Boolean>>() {
				});
	}

	private ConfigInfo localPreference(String groupId, String dataId) {
		final String parenPath = PathUtils.finalPath(PathConstants.FILE_LOCAL_PREF_PATH,
				namespaceId);
		final byte[] content = DiskUtils.readFileBytes(parenPath,
				NameUtils.buildName(groupId, dataId));
		if (Objects.nonNull(content) && content.length > 0) {
			return serializer.deserialize(content, ConfigInfo.class);
		}
		return null;
	}

	private ConfigInfo snapshotLoad(String groupId, String dataId) {
		final String parenPath = PathUtils.finalPath(PathConstants.FILE_CACHE_PATH,
				namespaceId);
		final String fileName = NameUtils.buildName(groupId, dataId);
		final byte[] content = DiskUtils.readFileBytes(parenPath, fileName);
		if (Objects.nonNull(content) && content.length > 0) {
			return serializer.deserialize(content, ConfigInfo.class);
		}
		return null;
	}

	private void snapshotSave(String groupId, String dataId, ConfigInfo configInfo) {
		final String parenPath = PathUtils.finalPath(PathConstants.FILE_CACHE_PATH,
				namespaceId);
		final String fileName = NameUtils.buildName(groupId, dataId);
		DiskUtils.writeFile(parenPath, fileName, GsonUtils.toJsonBytes(configInfo));
	}

	@Override
	public void destroy() {
		if (isInited() && destroyed.compareAndSet(false, true)) {
			LifeCycleHelper.invokeDestroy(httpClient);
			LifeCycleHelper.invokeDestroy(watchWorker);

			// help gc
			watchWorker = null;
			httpClient = null;
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
