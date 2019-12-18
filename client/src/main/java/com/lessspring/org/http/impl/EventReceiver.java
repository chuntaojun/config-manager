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

import java.util.Objects;

import com.google.common.eventbus.Subscribe;
import okhttp3.sse.EventSource;

/**
 * For the use of user-defined SSE the receiving processor
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public abstract class EventReceiver<T> {

	private EventSource eventSource;

	public EventReceiver() {
	}

	/**
	 * Data receiving callback function
	 *
	 * @param data T
	 */
	@Subscribe
	public abstract void onReceive(T data);

	/**
	 * When the error occurs when the callback function
	 *
	 * @param throwable {@link Throwable}
	 */
	public abstract void onError(Throwable throwable);

	/**
	 * Well what the receiver
	 *
	 * @return attention event name
	 */
	public abstract String attention();

	final void setEventSource(EventSource eventSource) {
		this.eventSource = eventSource;
	}

	// Cancel the sse request

	public final void cancle() {
		if (Objects.nonNull(eventSource)) {
			eventSource.cancel();
		}
	}
}
