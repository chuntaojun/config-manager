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

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.reflect.TypeToken;
import com.lessspring.org.Configuration;
import com.lessspring.org.auth.AuthHolder;
import com.lessspring.org.cluster.ClusterChoose;
import com.lessspring.org.constant.StringConst;
import com.lessspring.org.http.HttpClient;
import com.lessspring.org.http.MaxRetryException;
import com.lessspring.org.http.Retry;
import com.lessspring.org.http.handler.RequestHandler;
import com.lessspring.org.http.handler.ResponseHandler;
import com.lessspring.org.http.param.Body;
import com.lessspring.org.http.param.Header;
import com.lessspring.org.http.param.HttpMethod;
import com.lessspring.org.http.param.Query;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.utils.HttpUtils;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSources;
import org.apache.commons.lang3.StringUtils;

import static com.lessspring.org.constant.Code.SERVER_BUSY;
import static com.lessspring.org.constant.Code.UNAUTHORIZED;
import static com.lessspring.org.http.param.MediaType.APPLICATION_JSON_UTF8_VALUE;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
class ConfigHttpClient implements HttpClient {

	private OkHttpClient client;

	private final RequestHandler requestHandler = RequestHandler.getHandler();

	private final ResponseHandler responseHandler = ResponseHandler.getHandler();

	private final ClusterChoose choose;

	private final AuthHolder authHolder;

	private final Configuration configuration;

	private AtomicReference<String> clusterIp = new AtomicReference<>();

	ConfigHttpClient(ClusterChoose choose, AuthHolder authHolder,
			Configuration configuration) {
		this.choose = choose;
		this.authHolder = authHolder;
		this.configuration = configuration;
	}

	@Override
	public void init() {
		client = new OkHttpClient.Builder().connectTimeout(Duration.ofMillis(20_000))
				.readTimeout(Duration.ofMillis(30_000)).build();
	}

	@Override
	public <T> ResponseData<T> get(String url, Header header, Query query,
			TypeToken<ResponseData<T>> token) {
		Retry<ResponseData<T>> retry = new Retry<ResponseData<T>>() {
			@Override
			protected ResponseData<T> run() throws Exception {
				Request request = buildRequest(buildUrl(url, query), header, Body.EMPTY,
						HttpMethod.GET);
				return execute(client.newCall(request), token);
			}

			@Override
			protected boolean shouldRetry(ResponseData<T> data, Throwable throwable) {
				return retryStrategy(data, throwable);
			}

			@Override
			protected int maxRetry() {
				return 3;
			}
		};
		try {
			return retry.work();
		}
		catch (MaxRetryException e) {
			return ResponseData.fail();
		}
	}

	@Override
	public <T> ResponseData<T> delete(String url, Header header, Query query,
			TypeToken<ResponseData<T>> token) {
		Retry<ResponseData<T>> retry = new Retry<ResponseData<T>>() {
			@Override
			protected ResponseData<T> run() throws Exception {
				Request request = buildRequest(buildUrl(url, query), header, Body.EMPTY,
						HttpMethod.DELETE);
				return execute(client.newCall(request), token);
			}

			@Override
			protected boolean shouldRetry(ResponseData<T> data, Throwable throwable) {
				return retryStrategy(data, throwable);
			}

			@Override
			protected int maxRetry() {
				return 3;
			}
		};
		try {
			return retry.work();
		}
		catch (MaxRetryException e) {
			return ResponseData.fail();
		}
	}

	@Override
	public <T> ResponseData<T> put(String url, Header header, Query query, Body body,
			TypeToken<ResponseData<T>> token) {
		Retry<ResponseData<T>> retry = new Retry<ResponseData<T>>() {
			@Override
			protected ResponseData<T> run() throws Exception {
				Request request = buildRequest(buildUrl(url, query), header, body,
						HttpMethod.PUT);
				return execute(client.newCall(request), token);
			}

			@Override
			protected boolean shouldRetry(ResponseData<T> data, Throwable throwable) {
				return retryStrategy(data, throwable);
			}

			@Override
			protected int maxRetry() {
				return 3;
			}
		};
		try {
			return retry.work();
		}
		catch (MaxRetryException e) {
			return ResponseData.fail();
		}

	}

	@Override
	public <T> ResponseData<T> post(String url, Header header, Query query, Body body,
			TypeToken<ResponseData<T>> token) {
		Retry<ResponseData<T>> retry = new Retry<ResponseData<T>>() {
			@Override
			protected ResponseData<T> run() throws Exception {
				Request request = buildRequest(buildUrl(url, query), header, body,
						HttpMethod.POST);
				return execute(client.newCall(request), token);
			}

			@Override
			protected boolean shouldRetry(ResponseData<T> data, Throwable throwable) {
				return retryStrategy(data, throwable);
			}

			@Override
			protected int maxRetry() {
				return 3;
			}
		};
		try {
			return retry.work();
		}
		catch (MaxRetryException e) {
			return ResponseData.fail();
		}
	}

	@Override
	public <T> void serverSendEvent(String url, Header header, Body body, Class<T> cls,
			EventReceiver receiver) {
		Retry<Void> retry = new Retry<Void>() {
			@Override
			protected Void run() throws Exception {
				RequestBody postBody = RequestBody.create(
						MediaType.parse(APPLICATION_JSON_UTF8_VALUE),
						requestHandler.handle(body.getData()));
				Request.Builder builder = new Request.Builder().url(buildUrl(url))
						.post(postBody);
				initHeader(header, builder);
				EventSource.Factory factory = EventSources.createFactory(client);
				EventSource source = factory.newEventSource(builder.build(),
						new ServerSentEventListener<T>(receiver, cls));
				receiver.setEventSource(source);
				return null;
			}

			@Override
			protected boolean shouldRetry(Void data, Throwable throwable) {
				return false;
			}

			@Override
			protected int maxRetry() {
				return 3;
			}
		};
		try {
			retry.work();
		}
		catch (MaxRetryException ignore) {
		}
	}

	@Override
	public void destroy() {
		ServerSentEventListener.clean();
	}

	@Override
	public boolean isInited() {
		return false;
	}

	@Override
	public boolean isDestroyed() {
		return false;
	}

	private <T> ResponseData<T> execute(Call call, TypeToken<ResponseData<T>> token)
			throws IOException {
		ResponseData<T> data;
		Response response = call.execute();
		data = responseHandler.convert(response.body().string(), token);
		return data == null ? ResponseData.fail() : data;
	}

	private String buildUrl(String url) {
		return buildUrl(url, Query.EMPTY);
	}

	private String buildUrl(String url, Query query) {
		String queryStr = "";
		if (!query.isEmpty()) {
			queryStr = "?" + query.toQueryUrl();
		}
		if (url.startsWith("/")) {
			return HttpUtils.buildBasePath(getServerIp(), url + queryStr);
		}
		return HttpUtils.buildBasePath(getServerIp(), "/" + url + queryStr);
	}

	private Request buildRequest(String url, Header header, Body body,
			HttpMethod method) {
		Request.Builder builder = new Request.Builder();
		builder = builder.url(url);
		if (method == HttpMethod.GET) {
			builder = builder.get();
		}
		else if (method == HttpMethod.DELETE) {
			builder = builder.delete();
		}
		else if (method == HttpMethod.PUT) {
			RequestBody putBody = RequestBody.create(
					MediaType.parse(APPLICATION_JSON_UTF8_VALUE),
					requestHandler.handle(body.getData()));
			builder = builder.put(putBody);
		}
		else if (method == HttpMethod.POST) {
			RequestBody postBody = RequestBody.create(
					MediaType.parse(APPLICATION_JSON_UTF8_VALUE),
					requestHandler.handle(body.getData()));
			builder = builder.post(postBody);
		}
		else {
			throw new IllegalArgumentException("Does not support HTTP request type");
		}
		initHeader(header, builder);
		return builder.build();
	}

	private void initHeader(final Header header, Request.Builder builder) {
		Iterator<Map.Entry<String, String>> iterator = header.iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, String> entry = iterator.next();
			builder.addHeader(entry.getKey(), entry.getValue());
		}
		builder.addHeader(StringConst.CLIENT_ID_NAME, configuration.getClientId());
		builder.addHeader(StringConst.TOKEN_HEADER_NAME, authHolder.getToken());
	}

	private String getServerIp() {
		String ip = clusterIp.get();
		if (StringUtils.isEmpty(ip)) {
			clusterIp.set(choose.getLastClusterIp());
		}
		return clusterIp.get();
	}

	private void refresh() {
		clusterIp.set("");
	}

	private boolean retryStrategy(ResponseData<?> data, Throwable throwable) {
		boolean canRetry = false;
		if (Objects.nonNull(throwable)) {
			if (throwable instanceof SocketTimeoutException) {
				canRetry = true;
			}
			else if (throwable instanceof ConnectException) {
				refresh();
				canRetry = true;
			}
		}
		else {
			if (Objects.isNull(data)) {
				canRetry = true;
			}
			else {
				int code = data.getCode();
				if (code == UNAUTHORIZED.getCode()) {
					authHolder.refresh();
					canRetry = true;
				}
				else if (code == SERVER_BUSY.getCode()) {
					refresh();
					canRetry = true;
				}
			}
		}
		return canRetry;
	}

}
