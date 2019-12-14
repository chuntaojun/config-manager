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

import com.lessspring.org.constant.StringConst;
import com.lessspring.org.server.web.Router;
import de.codecentric.boot.admin.server.web.PathUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.result.condition.PatternsRequestCondition;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019/12/14 11:08 下午
 */
public class ConfRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

	@Override
	protected boolean isHandler(Class<?> beanType) {
		return AnnotatedElementUtils.hasAnnotation(beanType, Router.class);
	}

	@Override
	protected void registerHandlerMethod(Object handler, Method method,
			RequestMappingInfo mapping) {
		super.registerHandlerMethod(handler, method, withPrefix(mapping));
	}

	private RequestMappingInfo withPrefix(RequestMappingInfo mapping) {
		if (!StringUtils.hasText(StringConst.API_V1)) {
			return mapping;
		}
		PatternsRequestCondition patternsCondition = new PatternsRequestCondition(
				withNewPatterns(mapping.getPatternsCondition().getPatterns()));
		return new RequestMappingInfo(patternsCondition, mapping.getMethodsCondition(),
				mapping.getParamsCondition(), mapping.getHeadersCondition(),
				mapping.getConsumesCondition(), mapping.getProducesCondition(),
				mapping.getCustomCondition());
	}

	private List<PathPattern> withNewPatterns(Set<PathPattern> patterns) {
		return patterns.stream()
				.map(pattern -> getPathPatternParser()
						.parse(PathUtils.normalizePath(StringConst.API_V1 + pattern)))
				.collect(toList());
	}

}
