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

import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.reflect.TypeToken;
import com.lessspring.org.Configuration;
import com.lessspring.org.LifeCycleHelper;
import com.lessspring.org.auth.AuthHolder;
import com.lessspring.org.cluster.ClusterChoose;
import com.lessspring.org.http.HttpClient;
import com.lessspring.org.http.param.Body;
import com.lessspring.org.http.param.Header;
import com.lessspring.org.http.param.Query;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.server.utils.MetricsMonitor;
import io.prometheus.client.Histogram;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class MetricsHttpClient implements HttpClient {

	private HttpClient client;

	private final AtomicBoolean inited = new AtomicBoolean(false);
	private final AtomicBoolean destroyed = new AtomicBoolean(false);

	public MetricsHttpClient(ClusterChoose choose, AuthHolder authHolder,
			Configuration configuration) {
		client = new ConfigHttpClient(choose, authHolder, configuration);
	}

	@Override
	public <T> ResponseData<T> get(String url, Header header, Query query,
			TypeToken<ResponseData<T>> token) {
		Histogram.Timer timer = MetricsMonitor.getRequestMonitor("GET", url, "NA");
		ResponseData<T> response = null;
		try {
			response = client.get(url, header, query, token);
		}
		finally {
			timer.observeDuration();
			timer.close();
		}
		return response;
	}

	@Override
	public <T> ResponseData<T> delete(String url, Header header, Query query,
			TypeToken<ResponseData<T>> token) {
		Histogram.Timer timer = MetricsMonitor.getRequestMonitor("DELETE", url, "NA");
		ResponseData<T> response = null;
		try {
			response = client.delete(url, header, query, token);
		}
		finally {
			timer.observeDuration();
			timer.close();
		}
		return response;
	}

	@Override
	public <T> ResponseData<T> put(String url, Header header, Query query, Body body,
			TypeToken<ResponseData<T>> token) {
		Histogram.Timer timer = MetricsMonitor.getRequestMonitor("PUT", url, "NA");
		ResponseData<T> response = null;
		try {
			response = client.put(url, header, query, body, token);
		}
		finally {
			timer.observeDuration();
			timer.close();
		}
		return response;
	}

	@Override
	public <T> ResponseData<T> post(String url, Header header, Query query, Body body,
			TypeToken<ResponseData<T>> token) {
		Histogram.Timer timer = MetricsMonitor.getRequestMonitor("PUT", url, "NA");
		ResponseData<T> response = null;
		try {
			response = client.post(url, header, query, body, token);
		}
		finally {
			timer.observeDuration();
			timer.close();
		}
		return response;
	}

	@SuppressWarnings("all")
	@Override
	public <T> void serverSendEvent(String url, Header header, Body body, Class<T> cls,
			EventReceiver receiver) {
		client.serverSendEvent(url, header, body, cls, receiver);
	}

	@Override
	public void init() {
		if (inited.compareAndSet(false, true)) {
			LifeCycleHelper.invokeInit(client);
		}
	}

	@Override
	public void destroy() {
		if (isInited() && destroyed.compareAndSet(false, true)) {
			LifeCycleHelper.invokeDestroy(client);
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
