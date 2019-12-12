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

package com.lessspring.org.watch.longpoll;

import com.google.gson.reflect.TypeToken;
import com.lessspring.org.CacheConfigManager;
import com.lessspring.org.Configuration;
import com.lessspring.org.HashUtils;
import com.lessspring.org.NameUtils;
import com.lessspring.org.api.ApiConstant;
import com.lessspring.org.constant.WatchType;
import com.lessspring.org.filter.ConfigFilterManager;
import com.lessspring.org.http.HttpClient;
import com.lessspring.org.http.param.Body;
import com.lessspring.org.http.param.Header;
import com.lessspring.org.http.param.Query;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.model.vo.WatchRequest;
import com.lessspring.org.pojo.CacheItem;
import com.lessspring.org.watch.AbstractWatchWorker;
import okhttp3.OkHttpClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019-12-01 13:21
 */
public class LongPollWatchConfigWorker extends AbstractWatchWorker {

	private List<SubWorker> workers = new ArrayList<>(8);

	/**
	 * -1 onChange -2 destroy -3 free
	 */
	private int sign = -3;

	public LongPollWatchConfigWorker(HttpClient httpClient, Configuration configuration,
			ConfigFilterManager configFilterManager) {
		super(httpClient, configuration, configFilterManager, WatchType.LONG_POLL);
	}

	@Override
	public void onChange() {
		sign = -1;
		destroy();
		init();
		sign = -3;
	}

	@Override
	public void createWatcher() {
		for (int i = 0; i < workers.size(); i++) {
			SubWorker worker = new SubWorker(i, httpClient);
			workers.add(worker);
		}
		executor.schedule(() -> {
			for (SubWorker worker : workers) {
				worker.init();
			}
		}, 5, TimeUnit.SECONDS);
	}

	@Override
	public void init() {
		executor.schedule(() -> {
			for (SubWorker worker : workers) {
				worker.init();
			}
		}, 5, TimeUnit.SECONDS);
	}

	@Override
	public void destroy() {
		// If you are in a free state, call the destroy callback and end the task
		// directly
		if (sign == -3) {
			sign = -2;
		}
		for (SubWorker worker : workers) {
			worker.destroy();
		}
	}

	private class SubWorker implements Runnable {

		/**
		 * 通过index分发data-id监听，如果index为-1，则关闭任务执行
		 */
		private int index;
		private final HttpClient client;
		private final CacheConfigManager configManager = LongPollWatchConfigWorker.this.configManager;
		private final Map<String, CacheItem> observerMap = new HashMap<>();
		private final OkHttpClient okHttpClient;
		private final long longPollTime = configuration.getLongPollTime().getSeconds();
		private final Object monitor = new Object();

		SubWorker(int index, HttpClient client) {
			this.index = index;
			this.client = client;
			this.okHttpClient = new OkHttpClient.Builder()
					.connectTimeout(Duration.ofMillis(20_000))
					.readTimeout(Duration.ofSeconds(longPollTime)).build();
		}

		@Override
		public void run() {
			if (sign == -2) {
				return;
			}
			if (sign == -1) {
				synchronized (monitor) {
					try {
						monitor.wait();
					}
					catch (InterruptedException ignore) {
					}
				}
			}

			final Map<String, String> watchInfo = observerMap.entrySet().stream().collect(
					HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue().getLastMd5()),
					HashMap::putAll);
			final WatchRequest request = new WatchRequest(configuration.getNamespaceId(),
					watchInfo);
			final Body body = Body.objToBody(request);

			final Header header = Header.newInstance().addParam("hold-time",
					longPollTime + "s");

			ResponseData<List<String>> responseData = client.post(okHttpClient,
					ApiConstant.WATCH_CONFIG_LONG_POLL, header, Query.EMPTY, body,
					new TypeToken<ResponseData<List<String>>>() {
					});

			if (responseData.ok()) {
				List<String> changeDataId = responseData.getData();
				for (String s : changeDataId) {
					String[] infos = NameUtils.splitName(s);
					ConfigInfo configInfo = configManager.query(infos[0], infos[1], observerMap.get(s).getToken());
					notifyWatcher(configInfo);
				}
			}
			else {
				// TODO print error log
			}

			executor.execute(this);

		}

		public void init() {
			boolean needWeakUp = sign == -1;
			Map<String, CacheItem> cacheItemMap = configManager.copy();
			for (Map.Entry<String, CacheItem> entry : cacheItemMap.entrySet()) {
				if (HashUtils.distroHash(entry.getKey(), 8) == index) {
					observerMap.put(entry.getKey(), entry.getValue());
				}
			}
			if (needWeakUp) {
				synchronized (monitor) {
					monitor.notify();
				}
			}

			executor.execute(this);
		}

		public void destroy() {
			observerMap.clear();
		}
	}

}
