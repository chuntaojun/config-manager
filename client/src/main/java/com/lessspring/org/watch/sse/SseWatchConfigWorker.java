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
package com.lessspring.org.watch.sse;

import com.lessspring.org.Configuration;
import com.lessspring.org.LifeCycleHelper;
import com.lessspring.org.api.ApiConstant;
import com.lessspring.org.constant.WatchType;
import com.lessspring.org.filter.ConfigFilterManager;
import com.lessspring.org.http.HttpClient;
import com.lessspring.org.http.impl.EventReceiver;
import com.lessspring.org.http.param.Body;
import com.lessspring.org.http.param.Header;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.model.vo.WatchRequest;
import com.lessspring.org.model.vo.WatchResponse;
import com.lessspring.org.server.pojo.CacheItem;
import com.lessspring.org.watch.AbstractWatchWorker;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class SseWatchConfigWorker extends AbstractWatchWorker {

	private static Logger logger = Logger.getAnonymousLogger();

	private EventReceiver<WatchResponse> receiver;

	public SseWatchConfigWorker(HttpClient httpClient, Configuration configuration,
			ConfigFilterManager configFilterManager) {
		super(httpClient, configuration, configFilterManager, WatchType.SSE);
	}

	@Override
	public void init() {
		super.init();
	}

	@Override
	public void destroy() {
		super.destroy();
		receiver.cancle();
		receiver = null;
		LifeCycleHelper.invokeDestroy(httpClient);
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

	@Override
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

	// Create a Watcher to monitor configuration changes information

	@Override
	public void createWatcher() {
		Map<String, CacheItem> tmp = configManager.copy();
		Map<String, String> watchInfo = tmp.entrySet().stream().collect(HashMap::new,
				(m, e) -> m.put(e.getKey(), e.getValue().getLastMd5()), HashMap::putAll);
		final WatchRequest request = new WatchRequest(configuration.getNamespaceId(),
				watchInfo);
		final Body body = Body.objToBody(request);

		// Create a receiving server push transfer event receiver
		receiver = new EventReceiver<WatchResponse>() {

			private String name;

			@Override
			public void onReceive(WatchResponse data) {
				if (!data.isEmpty()) {
					final ConfigInfo configInfo = ConfigInfo.builder()
							.groupId(data.getGroupId()).dataId(data.getDataId())
							.content(data.getContent()).encryption(data.getEncryption())
							.file(data.getFile()).type(data.getType()).build();
					SseWatchConfigWorker.this.updateAndNotify(configInfo);
				}
			}

			@Override
			public void onError(Throwable throwable) {
				SseWatchConfigWorker.this.onError(throwable);
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
