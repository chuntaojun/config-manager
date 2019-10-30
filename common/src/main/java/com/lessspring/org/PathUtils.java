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

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class PathUtils {

	private static volatile boolean initialized = false;

	private static String FATHER_ROAD_KING = Paths
			.get(System.getProperty("user.home"), "config-manager").toString();

	public static void init(String path) {
		if (!initialized) {
			initialized = true;
			if (StringUtils.isNoneEmpty(path)) {
				FATHER_ROAD_KING = path;
			}
		}
		else {
			throw new IllegalStateException(
					"Initialization method can execute only once");
		}
	}

	public static String getFatherRoadKing() {
		return FATHER_ROAD_KING;
	}

	public static String finalPath(String... subPaths) {
		StringBuilder pathb = new StringBuilder();
		pathb.append(FATHER_ROAD_KING);
		for (String subPath : subPaths) {
			if (subPath.startsWith(File.separator)) {
				subPath = subPath.substring(1);
			}
			pathb.append(File.separator).append(subPath);
		}
		return pathb.toString();
	}

	public static String join(Object... objects) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < objects.length; i++) {
			builder.append(objects[i]);
			if (i != objects.length - 1) {
				builder.append(File.separator);
			}
		}
		return builder.toString();
	}
}
