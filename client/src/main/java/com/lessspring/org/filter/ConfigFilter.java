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

import com.lessspring.org.model.dto.ConfigInfo;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public interface ConfigFilter {

	public static final int HIGH_PRIORITY = Integer.MIN_VALUE;

	public static final int Low_PRIORITY = Integer.MAX_VALUE;

	/**
	 * this filter name
	 *
	 * @return name
	 */
	String name();

	/**
	 * The ConfigInfo do intercept processing
	 *
	 * @param configInfo {@link ConfigInfo}
	 */
	void filter(ConfigInfo configInfo);

	/**
	 * this filter priority
	 *
	 * @return int
	 */
	int priority();

}
