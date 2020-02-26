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
package com.conf.org;

import com.conf.org.api.ApiConstant;
import com.conf.org.constant.Code;
import com.conf.org.constant.WatchType;
import com.conf.org.model.dto.ConfigInfo;
import com.conf.org.model.vo.PublishConfigRequest;
import com.conf.org.model.vo.ResponseData;
import com.conf.org.utils.GsonUtils;
import com.google.common.collect.ImmutableMap;
import com.google.gson.reflect.TypeToken;
import com.conf.org.common.limit.RequestLimitManager;
import com.conf.org.filter.ConfigFilterManager;
import com.conf.org.http.HttpClient;
import com.conf.org.http.param.Body;
import com.conf.org.http.param.Header;
import com.conf.org.http.param.Query;
import com.conf.org.server.pojo.CacheItem;
import com.conf.org.server.utils.PathConstants;
import com.conf.org.watch.AbstractWatchWorker;
import com.conf.org.watch.ChangeKeyListener;
import com.conf.org.watch.WrapperListener;
import com.conf.org.watch.longpoll.LongPollWatchConfigWorker;
import com.conf.org.watch.sse.SseWatchConfigWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Manager of all actively pulled and monitored configuration items
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class CacheConfigManager implements LifeCycle {

	private static final Logger logger = LoggerFactory
			.getLogger(CacheConfigManager.class);

	private SerializerUtils serializer = SerializerUtils.getInstance();

	private ImmutableMap<String, CacheItem> itemImmutableMap;

	private HttpClient httpClient;
	private AbstractWatchWorker watchWorker;

	private final RequestLimitManager limitManager;
	private final Map<String, CacheItem> cacheItemMap;
	private final String namespaceId;
	private final boolean localPref;
	private final ConfigFilterManager configFilterManager;
	private final AtomicBoolean inited = new AtomicBoolean(false);
	private final AtomicBoolean destroyed = new AtomicBoolean(false);

	CacheConfigManager(HttpClient client, Configuration configuration,
			ConfigFilterManager configFilterManager) {
		logger.info("start");
		this.httpClient = client;
		this.limitManager = new RequestLimitManager(configuration);
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

	void registerListener(String groupId, String dataId, String encryption,
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
		return copy(true);
	}

	public Map<String, CacheItem> copy(boolean cache) {
		if (cache) {
			if (Objects.isNull(itemImmutableMap)) {
				synchronized (this) {
					itemImmutableMap = ImmutableMap.copyOf(cacheItemMap);
				}
			}
			return itemImmutableMap;
		}
		return new HashMap<>(cacheItemMap);
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
			if (limitManager.canSendRequest(NameUtils.buildName(groupId, dataId))) {
				final Query query = Query.newInstance()
						.addParam("namespaceId", namespaceId).addParam("groupId", groupId)
						.addParam("dataId", dataId).addParam(Constant.SHARE_ID_NAME,
								ShareIdUtils.buildShareId(namespaceId, groupId, dataId));
				ResponseData<ConfigInfo> response = httpClient.get(
						ApiConstant.QUERY_CONFIG, Header.EMPTY, query,
						new TypeToken<ResponseData<ConfigInfo>>() {
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
			else {
				return null;
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
		if (limitManager.canSendRequest(NameUtils.buildName(groupId, dataId))) {
			final Query query = Query.newInstance().addParam("namespaceId", namespaceId)
					.addParam("groupId", groupId).addParam("dataId", dataId)
					.addParam(Constant.SHARE_ID_NAME,
							ShareIdUtils.buildShareId(namespaceId, groupId, dataId));
			return httpClient.delete(ApiConstant.DELETE_CONFIG, Header.EMPTY, query,
					new TypeToken<ResponseData<Boolean>>() {
					});
		}
		return ResponseData.fail(Code.TRIGGER_CURRENT_LIMIT);
	}

	ResponseData<Boolean> publishConfig(final PublishConfigRequest request) {
		if (limitManager.canSendRequest(
				NameUtils.buildName(request.getGroupId(), request.getDataId()))) {
			final Query query = Query.newInstance().addParam("namespaceId", namespaceId)
					.addParam(Constant.SHARE_ID_NAME, ShareIdUtils.buildShareId(
							namespaceId, request.getGroupId(), request.getDataId()));
			return httpClient.put(ApiConstant.PUBLISH_CONFIG, Header.EMPTY, query,
					Body.objToBody(request), new TypeToken<ResponseData<Boolean>>() {
					});
		}
		return ResponseData.fail(Code.TRIGGER_CURRENT_LIMIT);
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
