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
package com.lessspring.org.common.parser;

import java.util.Map;
import java.util.function.Predicate;

import com.lessspring.org.model.dto.ConfigInfo;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
class JsonParser extends AbstraceParser {

	private Predicate<String> jsonPred = "json"::equalsIgnoreCase;

	@Override
	public Map<String, Object> toMap(ConfigInfo configInfo) {
		if (jsonPred.test(configInfo.getType())) {

		}
		return onNext(configInfo);
	}
}
