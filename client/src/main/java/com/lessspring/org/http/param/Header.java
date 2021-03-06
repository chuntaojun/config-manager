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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class Header {

	public static final Header EMPTY = Header.newInstance();

	private final Map<String, String> header;

	private Header() {
		header = new LinkedHashMap<String, String>();
		addParam("Content-Type", "application/json;charset=UTF-8");
		addParam("Accept-Charset", "UTF-8");
		addParam("Accept-Encoding", "gzip");
		addParam("Content-Encoding", "gzip");
	}

	public static Header newInstance() {
		return new Header();
	}

	public Header addParam(String key, String value) {
		header.put(key, value);
		return this;
	}

	public Header builded() {
		return this;
	}

	public String getValue(String key) {
		return header.get(key);
	}

	public Map<String, String> getHeader() {
		return header;
	}

	public Iterator<Map.Entry<String, String>> iterator() {
		return header.entrySet().iterator();
	}

	public List<String> toList() {
		List<String> list = new ArrayList<String>(header.size() * 2);
		Iterator<Map.Entry<String, String>> iterator = iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, String> entry = iterator.next();
			list.add(entry.getKey());
			list.add(entry.getValue());
		}
		return list;
	}

	public void addAll(List<String> list) {
		if ((list.size() & 1) != 0) {
			throw new IllegalArgumentException("list size must be a multiple of 2");
		}
		for (int i = 0; i < list.size();) {
			header.put(list.get(i++), list.get(i++));
		}
	}

	public void initHeader(Map<String, String> params) {
		for (Map.Entry<String, String> entry : params.entrySet()) {
			addParam(entry.getKey(), entry.getValue());
		}
	}

	public void clear() {
		header.clear();
	}

}
