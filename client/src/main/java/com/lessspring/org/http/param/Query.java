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
package com.lessspring.org.http.param;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class Query {

	private boolean isEmpty = true;

	public static final Query EMPTY = Query.newInstance();

	private Map<String, Object> params;

	public Query() {
		params = new LinkedHashMap<String, Object>();
	}

	public static Query newInstance() {
		return new Query();
	}

	public Query addParam(String key, Object value) {
		isEmpty = false;
		params.put(key, value);
		return this;
	}

	public Object getValue(String key) {
		return params.get(key);
	}

	public void initParams(Map<String, String> params) {
		for (Map.Entry<String, String> entry : params.entrySet()) {
			addParam(entry.getKey(), entry.getValue());
		}
	}

	public void initParams(List<String> list) {
		if ((list.size() & 1) != 0) {
			throw new IllegalArgumentException("list size must be a multiple of 2");
		}
		for (int i = 0; i < list.size();) {
			addParam(list.get(i++), list.get(i++));
		}
	}

	public String toQueryUrl() {
		StringBuilder urlBuilder = new StringBuilder();
		Set<Map.Entry<String, Object>> entrySet = params.entrySet();
		int i = entrySet.size();
		for (Map.Entry<String, Object> entry : entrySet) {
			try {
				urlBuilder.append(entry.getKey()).append("=").append(
						URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8"));
				if (i > 1) {
					urlBuilder.append("&");
				}
				i--;
			}
			catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}

		return urlBuilder.toString();
	}

	public void clear() {
		isEmpty = false;
		params.clear();
	}

	public boolean isEmpty() {
		return isEmpty;
	}

}
