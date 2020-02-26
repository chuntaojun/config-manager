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

import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public abstract class AbstraceParser implements Parser {

	protected static final String DOT = ".";

	protected static final String VALUE = "value";

	private AbstraceParser next;

	final void setNext(AbstraceParser next) {
		this.next = next;
	}

	public final Map<String, Object> onNext(ConfigInfo configInfo) {
		return next == null ? Collections.emptyMap() : next.toMap(configInfo);
	}

}