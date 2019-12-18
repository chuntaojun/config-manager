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

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lessspring.org.EncryptionUtils;
import com.lessspring.org.constant.StringConst;
import com.lessspring.org.model.dto.ConfigInfo;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class PlaceholderProcessor {

	private final Pattern encryptionPattern;
	private final Pattern decryptionPattern;

	public PlaceholderProcessor() {
		encryptionPattern = Pattern.compile(StringConst.ENCRYPTION_PLACEHOLDER);
		decryptionPattern = Pattern.compile(StringConst.DECRYPTION_PLACEHOLDER);
	}

	public void encryption(Optional<ConfigInfo> configInfoOptional) {
		configInfoOptional.ifPresent(configInfo -> {
			String token = configInfo.getEncryption();
			String content = configInfo.getContent();
			if (StringUtils.isEmpty(content)) {
				return;
			}
			Matcher matcher = encryptionPattern.matcher(content);
			while (matcher.find()) {
				String target = matcher.group(0);
				String value = target.replace("ENC{", "").replace("}", "");
				String encryptTxt = EncryptionUtils.encrypt(value, token);
				String replace = "DECR{" + encryptTxt + "}";
				content = content.replace(target, replace);
			}
			configInfo.setContent(content);
		});
	}

	public void decryption(Optional<ConfigInfo> configInfoOptional, String token) {
		configInfoOptional.ifPresent(configInfo -> {
			String content = configInfo.getContent();
			if (StringUtils.isAnyEmpty(content, token)) {
				return;
			}
			Matcher matcher = decryptionPattern.matcher(content);
			while (matcher.find()) {
				String target = matcher.group(0);
				String value = target.replace("DECR{", "").replace("}", "");
				String replace = EncryptionUtils.decrypt(value, token);
				content = content.replace(target, replace);
			}
			configInfo.setContent(content);
		});
	}

}
