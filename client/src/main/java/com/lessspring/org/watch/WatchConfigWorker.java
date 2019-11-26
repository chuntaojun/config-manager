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
package com.lessspring.org.watch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.lessspring.org.AbstractListener;
import com.lessspring.org.CacheConfigManager;
import com.lessspring.org.ClassLoaderSwitcherUtils;
import com.lessspring.org.Configuration;
import com.lessspring.org.LifeCycle;
import com.lessspring.org.LifeCycleHelper;
import com.lessspring.org.NameUtils;
import com.lessspring.org.api.ApiConstant;
import com.lessspring.org.executor.NameThreadFactory;
import com.lessspring.org.executor.ThreadPoolHelper;
import com.lessspring.org.filter.ConfigFilterManager;
import com.lessspring.org.http.HttpClient;
import com.lessspring.org.http.impl.EventReceiver;
import com.lessspring.org.http.param.Body;
import com.lessspring.org.http.param.Header;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.model.vo.WatchRequest;
import com.lessspring.org.model.vo.WatchResponse;
import com.lessspring.org.pojo.CacheItem;
import com.lessspring.org.utils.MD5Utils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class WatchConfigWorker implements LifeCycle {

	private static Logger logger = Logger.getAnonymousLogger();

	private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
			Runtime.getRuntime().availableProcessors(),
			new NameThreadFactory("com.lessspring.org.config-manager.client.watcher-"));

	private CacheConfigManager configManager;
	private final HttpClient httpClient;
	private final Configuration configuration;
	private final ConfigFilterManager configFilterManager;
	private EventReceiver<WatchResponse> receiver;

	public WatchConfigWorker(HttpClient httpClient, Configuration configuration,
			ConfigFilterManager configFilterManager) {
		this.httpClient = httpClient;
		this.configuration = configuration;
		this.configFilterManager = configFilterManager;
	}

	@Override
	public void init() {
	}

	public void setConfigManager(CacheConfigManager configManager) {
		this.configManager = configManager;
		executor.schedule(this::createWatcher, 1000, TimeUnit.MILLISECONDS);
	}

	private void notifyWatcher(ConfigInfo configInfo) {
		final String groupId = configInfo.getGroupId();
		final String dataId = configInfo.getDataId();
		String key = NameUtils.buildName(groupId, dataId);
		List<AbstractListener> listeners = configManager.allListener(key);

		// do some processor to configInfo by filter chain
		configFilterManager.doFilter(configInfo);

		for (AbstractListener listener : listeners) {
			Runnable job = () -> {
				// In order to make the spi mechanisms can work better
				ClassLoaderSwitcherUtils.change(listener);
				listener.onReceive(configInfo);
				ClassLoaderSwitcherUtils.rollBack();
			};
			Executor userExecutor = listener.executor();
			if (Objects.isNull(userExecutor)) {
				job.run();
			}
			else {
				userExecutor.execute(job);
			}
		}
	}

	@Override
	public void destroy() {
		receiver.cancle();
		receiver = null;
		configManager = null;
		LifeCycleHelper.invokeDestroy(httpClient);
		ThreadPoolHelper.invokeShutdown(executor);
	}

	@Override
	public boolean isInited() {
		return false;
	}

	@Override
	public boolean isDestroyed() {
		return false;
	}

	// When your listener list changes, to build a monitoring events and
	// initiate Watch requests to the server

	public void onChange() {
		if (Objects.nonNull(receiver)) {
			receiver.cancle();
			receiver = null;
		}
		executor.schedule(this::createWatcher, 1000, TimeUnit.MILLISECONDS);
	}

	private void onError(Throwable throwable) {
		logger.warning(throwable.getMessage());
	}

	private void updateAndNotify(ConfigInfo configInfo) {
		final String groupId = configInfo.getGroupId();
		final String dataId = configInfo.getDataId();
		final String lastMd5 = MD5Utils.md5Hex(configInfo.toBytes());
		final CacheItem oldItem = configManager.getCacheItem(groupId, dataId);
		if (Objects.nonNull(oldItem) && oldItem.isChange(lastMd5)) {
			oldItem.setLastMd5(lastMd5);
			notifyWatcher(configInfo);
		}
	}

	// Create a Watcher to monitor configuration changes information

	private void createWatcher() {
		Map<String, CacheItem> tmp = configManager.copy();
		Map<String, String> watchInfo = tmp.entrySet().stream().collect(HashMap::new,
				(m, e) -> m.put(e.getKey(), e.getValue().getLastMd5()), HashMap::putAll);
		final WatchRequest request = new WatchRequest(configuration.getNamespaceId(),
				watchInfo);
		final Body body = Body.objToBody(request);

		// Create a receiving server push change event receiver
		receiver = new EventReceiver<WatchResponse>() {

			private String name;

			@Override
			public void onReceive(WatchResponse data) {
				if (!data.isEmpty()) {
					final ConfigInfo configInfo = ConfigInfo.builder()
							.groupId(data.getGroupId()).dataId(data.getDataId())
							.content(data.getContent()).encryption(data.getEncryption())
							.file(data.getFile()).type(data.getType()).build();
					WatchConfigWorker.this.updateAndNotify(configInfo);
				}
			}

			@Override
			public void onError(Throwable throwable) {
				WatchConfigWorker.this.onError(throwable);
			}

			@Override
			public String attention() {
				if (StringUtils.isEmpty(name)) {
					name = WatchResponse.class.getName();
				}
				return name;
			}
		};
		try {
			final Header header = Header.newInstance()
					.addParam("Accept", "text/event-stream")
					.addParam("Cache-Control", "no-cache");
			httpClient.serverSendEvent(ApiConstant.WATCH_CONFIG, header, body,
					WatchResponse.class, receiver);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
