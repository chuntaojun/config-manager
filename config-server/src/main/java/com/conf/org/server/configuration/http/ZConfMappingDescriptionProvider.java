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

package com.conf.org.server.configuration.http;

import com.conf.org.ReflectUtils;
import com.conf.org.server.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.web.mappings.HandlerMethodDescription;
import org.springframework.boot.actuate.web.mappings.MappingDescriptionProvider;
import org.springframework.boot.actuate.web.mappings.reactive.DispatcherHandlerMappingDescription;
import org.springframework.boot.actuate.web.mappings.reactive.DispatcherHandlerMappingDetails;
import org.springframework.boot.actuate.web.mappings.reactive.DispatcherHandlersMappingDescriptionProvider;
import org.springframework.boot.actuate.web.mappings.reactive.RequestMappingConditionsDescription;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.support.RouterFunctionMapping;
import org.springframework.web.reactive.result.condition.HeadersRequestCondition;
import org.springframework.web.reactive.result.condition.PatternsRequestCondition;
import org.springframework.web.reactive.result.condition.RequestMethodsRequestCondition;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.util.pattern.PathPattern;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019/12/16 7:35 下午
 */
@Slf4j
@SuppressWarnings("all")
@Order(value = Ordered.LOWEST_PRECEDENCE)
public class ZConfMappingDescriptionProvider
		extends DispatcherHandlersMappingDescriptionProvider
		implements MappingDescriptionProvider {

	private final Map<String, List<DispatcherHandlerMappingDescription>> cache = new HashMap<>(
			32);

	@Override
	public String getMappingName() {
		return "dispatcherHandlers";
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
			Map<String, List<DispatcherHandlerMappingDescription>> result = super.describeMappings(
					context);
			result.forEach(
					new BiConsumer<String, List<DispatcherHandlerMappingDescription>>() {
						@Override
						public void accept(String key,
								List<DispatcherHandlerMappingDescription> descriptions) {
							descriptions.removeIf(description -> Objects
									.isNull(description.getDetails()));
							descriptions.removeIf(description -> description
									.getPredicate().contains("/page"));
							descriptions.forEach(description -> {
								final String predicate = description.getPredicate();
								ConfVisitor visitor = ConfVisitor.match(predicate);
								if (Objects.nonNull(visitor)) {
									DispatcherHandlerMappingDetails details = description
											.getDetails();
									buildCondition(visitor, details);
									buildHandlerMethod(visitor, details);
								}
							});
						}
					});
			cache.putAll(result);
		}
	}

	private void buildCondition(ConfVisitor visitor,
			DispatcherHandlerMappingDetails details) {
		RequestMappingConditionsDescription condition = details
				.getRequestMappingConditions();
		if (Objects.isNull(condition)) {
			if (visitor != null) {
				final Set<RequestMethod> methods = visitor.getMethods();
				final PathPattern pathPattern = ReflectUtils.newInstance(
						PathPattern.class, null, visitor.getPath(),
						SpringUtils.getBean(RouterFunctionMapping.class)
								.getPathPatternParser(),
						null);
				final PatternsRequestCondition patternsRequestCondition = new PatternsRequestCondition(
						pathPattern);
				final RequestMethodsRequestCondition requestCondition = new RequestMethodsRequestCondition(
						methods.toArray(new RequestMethod[0]));
				final HeadersRequestCondition headersRequestCondition = new HeadersRequestCondition(
						visitor.getHeaders().toArray(new String[0]));
				final RequestMappingInfo mappingInfo = new RequestMappingInfo(
						patternsRequestCondition, requestCondition, null,
						headersRequestCondition, null, null, null);
				condition = ReflectUtils.newInstance(
						RequestMappingConditionsDescription.class,
						new Class[] { RequestMappingInfo.class }, mappingInfo);
				ReflectUtils.inject(details, condition, "requestMappingConditions");
			}
		}
	}

	private void buildHandlerMethod(ConfVisitor visitor,
			DispatcherHandlerMappingDetails details) {
		HandlerMethodDescription methodDescription = details.getHandlerMethod();
		if (Objects.isNull(methodDescription)) {
			final HandlerFunction handlerFunction = visitor.getHandlerFunction();
			final HandlerMethod handlerMethod = new HandlerMethod(handlerFunction,
					ReflectUtils.getMethod(handlerFunction, "handle",
							ServerRequest.class));
			methodDescription = new HandlerMethodDescription(handlerMethod);
			ReflectUtils.inject(details, methodDescription, "handlerMethod");
		}
	}

}
