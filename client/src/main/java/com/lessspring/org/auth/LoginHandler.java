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
package com.lessspring.org.auth;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.gson.reflect.TypeToken;
import com.lessspring.org.Configuration;
import com.lessspring.org.LifeCycle;
import com.lessspring.org.api.ApiConstant;
import com.lessspring.org.http.HttpClient;
import com.lessspring.org.http.param.Body;
import com.lessspring.org.http.param.Header;
import com.lessspring.org.http.param.Query;
import com.lessspring.org.model.vo.JwtResponse;
import com.lessspring.org.model.vo.LoginRequest;
import com.lessspring.org.model.vo.ResponseData;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class LoginHandler implements Observer, LifeCycle {

	private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
			1, r -> {
				Thread thread = new Thread(r);
				thread.setDaemon(true);
				thread.setName("com.lessspring.org.config-manager.client.auth");
				return thread;
			});

	private final HttpClient httpClient;
	private final AuthHolder authHolder;
	private final Configuration configuration;

	public LoginHandler(HttpClient httpClient, AuthHolder authHolder,
			Configuration configuration) {
		this.httpClient = httpClient;
		this.authHolder = authHolder;
		this.configuration = configuration;
	}

	@Override
	public void init() {
		authHolder.register(this);
		firstLogin();
	}

	private void firstLogin() {
		createLoginWork(() -> {
		});
		executor.scheduleAtFixedRate(() -> createLoginWork(() -> {
		}), 15 * 60L, 30 * 60L, TimeUnit.SECONDS);
	}

	private void createLoginWork(Runnable call) {
		String username = configuration.getUsername();
		String password = configuration.getPassword();
		LoginRequest request = new LoginRequest();
		request.setUsername(username);
		request.setPassword(password);
		final Body body = Body.objToBody(request);
		ResponseData<JwtResponse> responseData = httpClient.post(ApiConstant.LOGIN,
				Header.EMPTY, Query.EMPTY, body,
				new TypeToken<ResponseData<JwtResponse>>() {
				});
		if (responseData.ok()) {
			JwtResponse jwt = responseData.getData();
			authHolder.updateToken(jwt);
			call.run();
		}
	}

	@Override
	public void destroy() {
		executor.shutdown();
	}

	@Override
	public void update(Observable o, Object arg) {
		CountDownLatch latch = (CountDownLatch) arg;
		createLoginWork(latch::countDown);
	}
}
