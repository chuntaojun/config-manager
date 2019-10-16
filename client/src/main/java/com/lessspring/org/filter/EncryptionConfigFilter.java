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
package com.lessspring.org.filter;

import java.util.Optional;

import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.utils.PlaceholderProcessor;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class EncryptionConfigFilter implements ConfigFilter {

	private final PlaceholderProcessor processor = new PlaceholderProcessor();

	@Override
	public String name() {
		return "encryption";
	}

	@Override
	public void filter(ConfigInfo configInfo) {
		Optional<ConfigInfo> optional = Optional.ofNullable(configInfo);
		processor.decryption(optional, optional.orElse(ConfigInfo.EMPTY).getEncryption());
	}

	@Override
	public int priority() {
		return HIGH_PRIORITY;
	}
}
