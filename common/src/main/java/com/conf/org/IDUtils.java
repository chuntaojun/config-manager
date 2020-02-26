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
package com.conf.org;

import com.conf.org.utils.ByteUtils;
import com.conf.org.utils.MD5Utils;
import com.conf.org.utils.StringUtils;

import java.util.Base64;
import java.util.UUID;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class IDUtils {

	private IDUtils() {
	}

	public static String generateUuid(String name) {
		return UUID.fromString(name).toString();
	}

	public static String generateUuid() {
		return UUID.randomUUID().toString();
	}

	public static String generateMd5(String name) {
		return MD5Utils.md5Hex(name);
	}

	public static String generateBase64(String name) {
		Base64.Encoder encoder = Base64.getEncoder();
		return StringUtils.newString4UTF8(encoder.encode(ByteUtils.toBytes(name)));
	}

}
