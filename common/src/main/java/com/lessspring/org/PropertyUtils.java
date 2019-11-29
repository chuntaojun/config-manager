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

package com.lessspring.org;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class PropertyUtils {

	private static Properties properties = new Properties();

	static {
		InputStream inputStream = null;
		try {
			String baseDir = PathUtils.join("conf", "application.properties");
			if (!StringUtils.isBlank(baseDir)) {
				inputStream = new FileInputStream(baseDir);
			}
			else {
				baseDir = PathUtils.join("application.properties");
				inputStream = PropertyUtils.class.getResourceAsStream(baseDir);
			}
			properties.load(inputStream);
		}
		catch (Exception e) {
		}
		finally {
			if (Objects.nonNull(inputStream)) {
				try {
					inputStream.close();
				}
				catch (IOException ignore) {
				}
			}
		}
	}

	public static String getProperty(String key) {
		return properties.getProperty(key);
	}

	public static String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	public static List<String> getPropertyList(String key) {
		List<String> valueList = new ArrayList<>();

		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			String value = properties.getProperty(key + "[" + i + "]");
			if (StringUtils.isBlank(value)) {
				break;
			}

			valueList.add(value);
		}

		return valueList;
	}

}