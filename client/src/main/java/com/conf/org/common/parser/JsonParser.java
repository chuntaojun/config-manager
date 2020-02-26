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
package com.conf.org.common.parser;

import com.conf.org.model.dto.ConfigInfo;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
class JsonParser extends AbstraceParser {

	private Predicate<String> jsonPred = "json"::equalsIgnoreCase;

	@Override
	public Map<String, Object> toMap(ConfigInfo configInfo) {
		if (jsonPred.test(configInfo.getType())) {
			return parseJSON2Map(configInfo.getContent());
		}
		return onNext(configInfo);
	}

	private Map<String, Object> parseJSON2Map(String json) {
		Map<String, Object> map = new HashMap<>(32);
		com.google.gson.JsonParser p = new com.google.gson.JsonParser();
		JsonElement jsonElement = p.parse(json);
		if (null == jsonElement) {
			return map;
		}
		parseJsonNode(map, jsonElement, "");
		return map;
	}

	private void parseJsonNode(Map<String, Object> jsonMap, JsonElement jsonElement,
			String parentKey) {
		if (jsonElement.isJsonObject()) {
			Set<Map.Entry<String, JsonElement>> es = jsonElement.getAsJsonObject()
					.entrySet();
			for (Map.Entry<String, JsonElement> entry : es) {
				parseJsonNode(jsonMap, entry.getValue(),
						StringUtils.isEmpty(parentKey) ? entry.getKey()
								: parentKey + DOT + entry.getKey());
			}
		}
		if (jsonElement.isJsonArray()) {
			JsonArray array = jsonElement.getAsJsonArray();
			int i = 0;
			for (JsonElement item : array) {
				parseJsonNode(jsonMap, item, parentKey + "[" + i + "]");
				i++;
			}
		}
		if (jsonElement.isJsonPrimitive() || jsonElement.isJsonNull()) {
			jsonMap.put(parentKey, jsonElement.toString());
		}
	}
}
