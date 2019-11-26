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

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class ClassLoaderSwitchUtils {

	private static ThreadLocal<ClassLoader> PRE_CLASS_LOADER_HOLDER = new ThreadLocal<>();

	public static void transfer(Object obj) {
		transfer(obj.getClass());
	}

	public static void transfer(Class<?> cls) {
		PRE_CLASS_LOADER_HOLDER.set(Thread.currentThread().getContextClassLoader());
		Thread.currentThread().setContextClassLoader(cls.getClassLoader());
	}

	public static void rollBack() {
		Thread.currentThread().setContextClassLoader(PRE_CLASS_LOADER_HOLDER.get());
		PRE_CLASS_LOADER_HOLDER.remove();
	}

}