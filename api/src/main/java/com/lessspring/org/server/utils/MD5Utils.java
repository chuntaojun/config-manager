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

import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class MD5Utils {

	public static String md5Hex(String s) {
		return DigestUtils.md5Hex(s);
	}

	public static String md5Hex(byte[] bytes) {
		return DigestUtils.md5Hex(bytes);
	}

	public static boolean compareMd5(String var1, String var2) {
		return Objects.equals(md5Hex(var1), md5Hex(var2));
	}

}
