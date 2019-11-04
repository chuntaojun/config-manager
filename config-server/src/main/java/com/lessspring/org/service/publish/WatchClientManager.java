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
package com.lessspring.org.service.publish;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;

import com.lessspring.org.NameUtils;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.model.vo.WatchRequest;
import com.lessspring.org.pojo.CacheItem;
import com.lessspring.org.pojo.ReadWork;
import com.lessspring.org.pojo.event.config.NotifyEvent;
import com.lessspring.org.pojo.event.config.NotifyEventHandler;
import com.lessspring.org.pojo.event.config.PublishLogEvent;
import com.lessspring.org.service.config.ConfigCacheItemManager;
import com.lessspring.org.utils.GsonUtils;
import com.lessspring.org.utils.SystemEnv;
import com.lessspring.org.utils.TracerUtils;
import com.lmax.disruptor.WorkHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.FluxSink;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component
public class WatchClientManager implements WorkHandler<NotifyEventHandler> {

	private final long parallelThreshold = 100;

	private final SystemEnv systemEnv = SystemEnv.getSingleton();

	private final TracerUtils tracer = TracerUtils.getSingleton();

	private long clientCnt = 0;
	private final Object monitor = new Object();
	private Map<String, Map<String, Set<WatchClient>>> watchClientManager = new ConcurrentHashMap<>(
			8);

	@Autowired
	@Lazy
	private ConfigCacheItemManager cacheItemManager;

	// Build with the Client corresponds to a monitored object is
	// used to monitor configuration changes

	public void createWatchClient(WatchRequest request, FluxSink<?> sink,
			ServerRequest serverRequest) {
		WatchClient client = WatchClient.builder()
				.clientIp(Objects
						.requireNonNull(
								serverRequest.exchange().getRequest().getRemoteAddress())
						.getHostString())
				.checkKey(request.getWatchKey()).namespaceId(request.getNamespaceId())
				.response(serverRequest.exchange().getResponse()).sink(sink).build();
		// When event creation is cancelled, automatic cancellation of client
		// on the server side corresponding to monitor object
		sink.onDispose(() -> {
			synchronized (monitor) {
				clientCnt--;
			}
			Map<String, Set<WatchClient>> namespaceWatcher = watchClientManager
					.getOrDefault(client.getNamespaceId(), Collections.emptyMap());
			for (Map.Entry<String, Set<WatchClient>> entry : namespaceWatcher
					.entrySet()) {
				entry.getValue().remove(client);
			}
		});
		Map<String, String> listenKeys = client.getCheckKey();
		// According to the monitoring configuration key, registered to a
		// different key corresponding to the listener list
		listenKeys.forEach((key, value) -> {
			Map<String, Set<WatchClient>> clientsMap = watchClientManager.computeIfAbsent(
					client.getNamespaceId(), s -> new ConcurrentHashMap<>(4));
			clientsMap.computeIfAbsent(key, s -> new CopyOnWriteArraySet<>());
			clientsMap.get(key).add(client);
		});
		synchronized (monitor) {
			clientCnt++;
			log.info("【WatchClientManager】now watchClient count is : {}", clientCnt);
		}
		// A quick comparison, listens for client direct access to the latest
		// configuration when registering for the first time
		doQuickCompare(client);
	}

	private void doQuickCompare(WatchClient watchClient) {
		Map<String, String> checkMd5 = watchClient.getCheckKey();
		checkMd5.forEach((key, value) -> {
			String[] info = NameUtils.splitName(key);
			final CacheItem cacheItem = cacheItemManager
					.queryCacheItem(watchClient.getNamespaceId(), info[0], info[1]);
			cacheItem.executeReadWork(new ReadWork() {
				@Override
				public void job() {
					String content = cacheItemManager
							.readCacheFromDisk(watchClient.getNamespaceId(), key);
					if (StringUtils.isEmpty(content)) {
						// To conduct a read operation, will update CacheItem
						// information
						ConfigInfo configInfo = cacheItemManager.loadConfigFromDB(
								watchClient.getNamespaceId(), info[0], info[1]);
						if (configInfo == null) {
							return;
						}
						content = GsonUtils.toJson(configInfo);
					}
					if (cacheItem.isBeta()
							&& cacheItem.canRead(watchClient.getClientIp())) {
						writeResponse(watchClient, GsonUtils.toJson(content));
					}
				}

				@Override
				public void onError(Exception exception) {

				}
			});
		});
	}

	// Send the event to the client

	@SuppressWarnings("unchecked")
	private void writeResponse(WatchClient client, Object data) {
		client.getSink().next(data);
	}

	// Use the event framework, receiving NotifyEvent events, the
	// configuration changes on delivery to the client

	// TODO Push the trajectory logging

	@Override
	public void onEvent(NotifyEventHandler eventHandler) throws Exception {
		NotifyEvent event = eventHandler.getEvent();
		final CacheItem cacheItem = cacheItemManager.queryCacheItem(
				event.getNamespaceId(), event.getGroupId(), event.getDataId());
		final String configInfoJson = cacheItemManager.readCacheFromDisk(
				event.getNamespaceId(), event.getGroupId(), event.getDataId());
		long[] finishWorks = new long[1];
		Set<Map.Entry<String, Set<WatchClient>>> set = watchClientManager
				.getOrDefault(event.getNamespaceId(), Collections.emptyMap()).entrySet();
		Stream<Map.Entry<String, Set<WatchClient>>> stream;
		if (clientCnt >= parallelThreshold) {
			stream = set.parallelStream();
		}
		else {
			stream = set.stream();
		}
		stream.flatMap(stringSetEntry -> stringSetEntry.getValue().stream())
				.forEach(client -> {
					// If it is beta configuration file, you need to check the
					// client IP information
					if (event.isBeta()) {
						if (cacheItem.canRead(client.getClientIp())) {
							return;
						}
					}
					try {
						writeResponse(client, configInfoJson);
						finishWorks[0]++;
						tracer.publishPublishEvent(PublishLogEvent.builder()
								.namespaceId(event.getNamespaceId())
								.groupId(event.getGroupId()).dataId(event.getDataId())
								.clientIp(client.getClientIp())
								.publishTime(System.currentTimeMillis()).build());
					}
					catch (Exception e) {
						log.error("[Notify WatchClient has Error] : {}", e.getMessage());
					}
				});
		log.info("total notify clients finish success is : {}", finishWorks[0]);
	}

}
