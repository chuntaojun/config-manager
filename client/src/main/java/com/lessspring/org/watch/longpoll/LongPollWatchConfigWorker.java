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
import com.lessspring.org.constant.StringConst;
import com.lessspring.org.constant.WatchType;
import com.lessspring.org.filter.ConfigFilterManager;
import com.lessspring.org.http.HttpClient;
import com.lessspring.org.http.param.Body;
import com.lessspring.org.http.param.Header;
import com.lessspring.org.http.param.Query;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.model.vo.WatchRequest;
import com.lessspring.org.server.pojo.CacheItem;
import com.lessspring.org.watch.AbstractWatchWorker;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019-12-01 13:21
 */
public class LongPollWatchConfigWorker extends AbstractWatchWorker {

	private static final Logger logger = LoggerFactory.getLogger(LongPollWatchConfigWorker.class);

	private List<SubWorker> workers = new ArrayList<>(8);

	private volatile WorkerState workerState = WorkerState.FREE;

	private final long longPollTime;

	private final OkHttpClient okHttpClient;

	public LongPollWatchConfigWorker(HttpClient httpClient, Configuration configuration,
			ConfigFilterManager configFilterManager) {
		super(httpClient, configuration, configFilterManager, WatchType.LONG_POLL);
		this.longPollTime = configuration.getLongPollTime().getSeconds();
		this.okHttpClient = new OkHttpClient.Builder()
				.connectTimeout(Duration.ofMillis(20_000))
				.readTimeout(Duration.ofSeconds(longPollTime)).build();
	}

	@Override
	public void onChange() {
		// 运行 => 暂停
		workerState = WorkerState.SUSPEND;
		destroy();
		createWatcher();
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
			workerState = WorkerState.RUNNING;
		}, 5, TimeUnit.SECONDS);
	}

	@Override
	public void init() {
	}

	@Override
	public void destroy() {
		// If you are in a free state, call the destroy callback and end the task
		// directly
		if (WorkerState.RUNNING.equals(workerState)
				|| WorkerState.FREE.equals(workerState)) {
			workerState = WorkerState.SHUTDOWN;
		}
		for (SubWorker worker : workers) {
			worker.destroy();
		}
	}

	private class SubWorker implements Runnable {

		/**
		 * 通过index分发data-id监听
		 */
		private int index;
		private final HttpClient client;
		private final CacheConfigManager configManager = LongPollWatchConfigWorker.this.configManager;
		private final Map<String, CacheItem> observerMap = new HashMap<>();
		private final Object monitor = new Object();
		private final long longPollTime = LongPollWatchConfigWorker.this.longPollTime;

		SubWorker(int index, HttpClient client) {
			this.index = index;
			this.client = client;
		}

		@Override
		public void run() {
			if (WorkerState.SHUTDOWN.equals(workerState)) {
				return;
			}

			// Wait for the rebuild listener action to complete

			if (WorkerState.SUSPEND.equals(workerState)) {
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

			// Make a request to compute a ClientId listener
			final Header header = Header.newInstance()
					.addParam("hold-time", longPollTime + "s")
					.addParam(StringConst.CLIENT_ID_NAME,
							configuration.getClientId() + ":sub_" + index);

			ResponseData<List<String>> responseData = client.post(okHttpClient,
					ApiConstant.WATCH_CONFIG_LONG_POLL, header, Query.EMPTY, body,
					new TypeToken<ResponseData<List<String>>>() {
					});

			if (responseData.ok()) {
				List<String> changeDataId = responseData.getData();
				for (String s : changeDataId) {
					String[] infos = NameUtils.splitName(s);
					ConfigInfo configInfo = configManager.query(infos[0], infos[1],
							observerMap.get(s).getToken());
					notifyWatcher(configInfo);
				}
			}
			else {
				// TODO print error log
				logger.error("[LongPoll] has some error : {}", responseData);
			}

			executor.execute(this);

		}

		public void init() {
			boolean needWeakUp = WorkerState.SUSPEND.equals(workerState);
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
