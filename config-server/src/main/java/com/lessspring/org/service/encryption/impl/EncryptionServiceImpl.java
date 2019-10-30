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
package com.lessspring.org.service.encryption.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lessspring.org.EncryptionUtils;
import com.lessspring.org.constant.StringConst;
import com.lessspring.org.model.vo.PublishConfigRequest;
import com.lessspring.org.service.encryption.EncryptionService;

import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Component(value = "encryptionService")
public class EncryptionServiceImpl implements EncryptionService {

	private final PlaceholderProcessor processor = new PlaceholderProcessor();

	@Override
	public void handle(PublishConfigRequest request) {
		final String content = request.getContent();
		final String encryption = request.getEncryption();
		// Configure the encryption operation
		request.setContent(processor.encryption(content, encryption));
	}

	public static class PlaceholderProcessor {

		private final Pattern encryptionPattern;
		private final Pattern decryptionPattern;

		public PlaceholderProcessor() {
			encryptionPattern = Pattern.compile(StringConst.ENCRYPTION_PLACEHOLDER);
			decryptionPattern = Pattern.compile(StringConst.DECRYPTION_PLACEHOLDER);
		}

		public String encryption(String content, String token) {
			Matcher matcher = encryptionPattern.matcher(content);
			while (matcher.find()) {
				String target = matcher.group(0);
				String value = target.replace("ENC{", "").replace("}", "");
				String encryptTxt = EncryptionUtils.encrypt(value, token);
				String replace = "DECR{" + encryptTxt + "}";
				content = content.replace(target, replace);
			}
			return content;
		}

		public String decryption(String content, String token) {
			Matcher matcher = decryptionPattern.matcher(content);
			while (matcher.find()) {
				String target = matcher.group(0);
				String value = target.replace("DECR{", "").replace("}", "");
				String replace = EncryptionUtils.decrypt(value, token);
				content = content.replace(target, replace);
			}
			return content;
		}

	}

}
