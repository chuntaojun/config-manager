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
package com.lessspring.org.http.impl;

import java.util.HashMap;
import java.util.Map;

import com.google.common.eventbus.EventBus;
import com.google.gson.reflect.TypeToken;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.utils.GsonUtils;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
class ServerSentEventListener<T> extends EventSourceListener {

	// Event the dispenser, according to the Receiver concern events, event distribution

	private static final Map<String, EventBus> eventBusMap = new HashMap<>();

	private static final Object monitor = new Object();

	private final EventReceiver<T> receiver;

	private Class<T> typeCls;

	public ServerSentEventListener(final EventReceiver<T> receiver, Class<T> cls) {
		super();
		this.receiver = receiver;
		typeCls = cls;
		EventBus eventBus = new EventBus("config-watch-event-publisher");
		eventBusMap.computeIfAbsent(receiver.attention(), s -> eventBus);
		eventBus.register(receiver);
	}

	@Override
	public void onClosed(@NotNull EventSource eventSource) {
		super.onClosed(eventSource);
		// When close the incident, automatic cancellation of the Receiver
		eventBusMap.get(receiver.attention()).unregister(receiver);
	}

	@Override
	public void onEvent(@NotNull EventSource eventSource, @Nullable String id,
			@Nullable String type, @NotNull String data) {
		ResponseData<String> result = GsonUtils.toObj(data,
				new TypeToken<ResponseData<String>>() {
				}.getType());
		// For event distribution
		eventBusMap.get(receiver.attention())
				.post(GsonUtils.toObj(result.getData(), typeCls));
	}

	@Override
	public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t,
			@Nullable Response response) {
		receiver.onError(t);
	}

	@Override
	public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
		super.onOpen(eventSource, response);
	}

	static void clean() {
		eventBusMap.clear();
	}

}
