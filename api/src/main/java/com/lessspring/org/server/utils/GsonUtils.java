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
package com.lessspring.org.server.utils;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class GsonUtils {

	private static final Gson GSON = new GsonBuilder().setLenient().create();

	public static String toJson(Object obj) {
		return GSON.toJson(obj);
	}

	public static byte[] toJsonBytes(Object obj) {
		return ByteUtils.toBytes(GSON.toJson(obj));
	}

	public static <T> T toObj(byte[] json, Class<T> cls) {
		return toObj(StringUtils.newString4UTF8(json), cls);
	}

	public static <T> T toObj(byte[] json, Type cls) {
		return toObj(StringUtils.newString4UTF8(json), cls);
	}

	public static <T> T toObj(String json, Class<T> cls) {
		return GSON.fromJson(json, cls);
	}

	public static <T> T toObj(String json, TypeToken typeToken) {
		return GSON.fromJson(json, typeToken.getType());
	}

	public static <T> T toObj(String json, Type cls) {
		return GSON.fromJson(json, cls);
	}

}
