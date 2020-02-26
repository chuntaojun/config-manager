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
package com.conf.org.filter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ServiceLoader;

import com.conf.org.model.dto.ConfigInfo;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class ConfigFilterManager {

	private final LinkedHashMap<String, ConfigFilter> configFilters = new LinkedHashMap<>();

	{
		List<ConfigFilter> filterList = new ArrayList<>();
		ServiceLoader<ConfigFilter> filters = ServiceLoader.load(ConfigFilter.class);
		for (ConfigFilter filter : filters) {
			filterList.add(filter);
		}
		filterList.sort(Comparator.comparingInt(ConfigFilter::priority));
		for (ConfigFilter filter : filterList) {
			configFilters.put(filter.name(), filter);
		}
	}

	public void doFilter(ConfigInfo configInfo) {
		for (ConfigFilter filter : configFilters.values()) {
			filter.filter(configInfo);
		}
	}

}
