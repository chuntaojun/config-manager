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
package com.lessspring.org.http.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The response body processor
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class ResponseHandler {

	private static final ResponseHandler HANDLER = new ResponseHandler();

	public static ResponseHandler getHandler() {
		return HANDLER;
	}

	private final Gson gson = new Gson();

	public <T> T convert(String s, Class<T> cls) {
		return gson.fromJson(s, cls);
	}

	public <T> T convert(String s, TypeToken<T> token) {
		try {
			return gson.fromJson(s, token.getType());
		}
		catch (Exception e) {
			return null;
		}
	}

}
