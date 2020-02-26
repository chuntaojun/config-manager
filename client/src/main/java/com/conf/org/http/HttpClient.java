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
package com.conf.org.http;

import com.conf.org.LifeCycle;
import com.conf.org.http.param.Body;
import com.conf.org.http.param.Header;
import com.conf.org.http.param.Query;
import com.conf.org.model.vo.ResponseData;
import com.google.gson.reflect.TypeToken;
import com.conf.org.http.impl.EventReceiver;
import okhttp3.OkHttpClient;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public interface HttpClient extends LifeCycle {

	/**
	 * http get
	 *
	 * @param url url
	 * @param header http header param
	 * @param query http query param
	 * @param token return type
	 * @return {@link ResponseData <T>}
	 */
	<T> ResponseData<T> get(String url, Header header, Query query,
			TypeToken<ResponseData<T>> token);

	/**
	 * http delete
	 *
	 * @param url url
	 * @param header http header param
	 * @param query http query param
	 * @param token return type
	 * @return {@link ResponseData<T>}
	 */
	<T> ResponseData<T> delete(String url, Header header, Query query,
			TypeToken<ResponseData<T>> token);

	/**
	 * http put
	 *
	 * @param url url
	 * @param header http header param
	 * @param query http query param
	 * @param body http body param
	 * @param token return type
	 * @return {@link ResponseData}
	 */
	<T> ResponseData<T> put(String url, Header header, Query query, Body body,
			TypeToken<ResponseData<T>> token);

	/**
	 * http post
	 *
	 * @param url url
	 * @param header http header param
	 * @param query http query param
	 * @param body http body param
	 * @param token return type
	 * @return {@link ResponseData}
	 */
	<T> ResponseData<T> post(String url, Header header, Query query, Body body,
			TypeToken<ResponseData<T>> token);

	/**
	 * http post
	 *
	 * @param client {@link OkHttpClient}
	 * @param url url
	 * @param header http header param
	 * @param query http query param
	 * @param body http body param
	 * @param token return type
	 * @return {@link ResponseData}
	 */
	<T> ResponseData<T> post(OkHttpClient client, String url, Header header, Query query,
			Body body, TypeToken<ResponseData<T>> token);

	/**
	 * server send event
	 *
	 * @param url url
	 * @param header http header param
	 * @param body http body param
	 * @param cls return type
	 * @param receiver {@link EventReceiver}
	 */
	@SuppressWarnings("all")
	<T> void serverSendEvent(String url, Header header, Body body, Class<T> cls,
			EventReceiver receiver);

}
