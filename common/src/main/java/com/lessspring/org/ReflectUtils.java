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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class ReflectUtils {

	public static void inject(Object source, Object value, String fieldName) {
		Class<?> cls = source.getClass();
		try {
			Field field = getFied(cls, fieldName);
			field.set(source, value);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static Field getFied(Class<?> cls, String fieldName) {
		Field field = null;
		try {
			field = cls.getDeclaredField(fieldName);
			field.setAccessible(true);
			return field;
		}
		catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T newInstance(Class<T> cls, Class[] type, Object... param) {
		try {
			Constructor<T> constructor = cls.getDeclaredConstructor(type);
			constructor.setAccessible(true);
			return constructor.newInstance(param);
		} catch (Exception ignore) {
			return null;
		}
	}

}
