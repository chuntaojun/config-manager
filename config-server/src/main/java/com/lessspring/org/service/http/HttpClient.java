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
package com.lessspring.org.service.http;

import java.io.IOException;
import java.time.Duration;

import com.google.gson.reflect.TypeToken;
import com.lessspring.org.LifeCycle;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.raft.vo.ServerNode;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class HttpClient implements LifeCycle {

	private OkHttpClient client;

	@Override
	public void init() {
		client = new OkHttpClient.Builder().connectTimeout(Duration.ofMillis(20_000))
				.readTimeout(Duration.ofMillis(30_000)).build();
	}

	public <T> void get(ServerNode node, String path, Headers headers, Query query,
			TypeToken<ResponseData<T>> token, Callback callback) {
	}

	public <T> void post(ServerNode node, String path, Headers headers, Query query,
			RequestBody requestBody, TypeToken<ResponseData<T>> token,
			Callback callback) {
	}

	public <T> void put(ServerNode node, String path, Headers headers, Query query,
			RequestBody requestBody, TypeToken<ResponseData<T>> token,
			Callback callback) {
	}

	public <T> void delete(ServerNode node, String path, Headers headers, Query query,
			TypeToken<ResponseData<T>> token, Callback callback) {
	}

	private <T> void execute(Call call, Callback callback,
			TypeToken<ResponseData<T>> token) throws IOException {
		call.enqueue(callback);
	}

	@Override
	public void destroy() {

	}
}
