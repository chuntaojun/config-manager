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

package com.lessspring.org.server.configuration;

import com.lessspring.org.ReflectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.web.mappings.MappingDescriptionProvider;
import org.springframework.boot.actuate.web.mappings.reactive.DispatcherHandlerMappingDescription;
import org.springframework.boot.actuate.web.mappings.reactive.DispatcherHandlerMappingDetails;
import org.springframework.boot.actuate.web.mappings.reactive.DispatcherHandlersMappingDescriptionProvider;
import org.springframework.boot.actuate.web.mappings.reactive.RequestMappingConditionsDescription;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.result.condition.RequestMethodsRequestCondition;
import org.springframework.web.reactive.result.method.RequestMappingInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019/12/16 7:35 下午
 */
@Slf4j
@SuppressWarnings("all")
public class ZConfMappingDescriptionProvider
		implements MappingDescriptionProvider {

	@Autowired
	private DispatcherHandlersMappingDescriptionProvider provider;

	private final Map<String, List<DispatcherHandlerMappingDescription>> cache = new HashMap<>(
			32);

	@Override
	public String getMappingName() {
		return "ZConfMappingDescriptionProvider";
	}

	@Override
	public Map<String, List<DispatcherHandlerMappingDescription>> describeMappings(
			ApplicationContext context) {
		init(context);
		return cache;
	}

	private final AtomicBoolean initialize = new AtomicBoolean(false);

	private void init(ApplicationContext context) {
		if (initialize.compareAndSet(false, true)) {
			Map<String, List<DispatcherHandlerMappingDescription>> result = provider.describeMappings(
					context);
			result.entrySet().stream().forEach(stringListEntry -> {
				List<DispatcherHandlerMappingDescription> descriptions = stringListEntry
						.getValue();
				descriptions.removeIf(
						description -> Objects.isNull(description.getDetails()));
				descriptions.forEach(description -> {
					final String predicate = description.getPredicate();
					ConfVisitor visitor = ConfVisitor.match(predicate);
					DispatcherHandlerMappingDetails details = description.getDetails();
					RequestMappingConditionsDescription condition = details
							.getRequestMappingConditions();
					if (Objects.isNull(condition)) {
						if (visitor != null) {
							final Set<RequestMethod> methods = visitor.getMethods();
							final RequestMethodsRequestCondition requestCondition = new RequestMethodsRequestCondition(
									methods.toArray(new RequestMethod[0]));
							RequestMappingInfo mappingInfo = new RequestMappingInfo(null,
									requestCondition, null, null, null, null, null);
							condition = ReflectUtils.newInstance(
									RequestMappingConditionsDescription.class,
									new Class[] { RequestMappingInfo.class },
									mappingInfo);
							ReflectUtils.inject(details, condition,
									"requestMappingConditions");
						}
					}
				});
				cache.put(stringListEntry.getKey(), descriptions);
			});
		}
	}
}
