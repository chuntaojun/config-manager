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

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019/12/16 9:15 下午
 */
@Slf4j
public class ConfVisitor implements RequestPredicates.Visitor {

	private static final Map<String, ConfVisitor> registerVisitor = new HashMap<>();

	private Set<RequestMethod> methods = new HashSet<>(3);
	private String path;
	private HandlerFunction<?> handlerFunction;
	private List<String> headers = new ArrayList<>();

    public Set<RequestMethod> getMethods() {
        return methods;
    }

    public HandlerFunction<?> getHandlerFunction() {
        return handlerFunction;
    }

    public String getPath() {
        return path;
    }

	public List<String> getHeaders() {
		return headers;
	}

	public static ConfVisitor match(String path) {
		for (Map.Entry<String, ConfVisitor> entry : registerVisitor.entrySet()) {
			final String url = entry.getKey();
			if (url.contains(path) || path.contains(url)) {
				return entry.getValue();
			}
		}
		return null;
	}

	public void setHandlerFunction(HandlerFunction<?> handlerFunction) {
		this.handlerFunction = handlerFunction;
	}

	@Override
	public void method(Set<HttpMethod> methods) {
		this.methods = methods.stream()
				.map(httpMethod -> RequestMethod.valueOf(httpMethod.name()))
				.collect(Collectors.toSet());
	}

	@Override
	public void path(String pattern) {
		this.path = pattern;
		registerVisitor.put(pattern, this);
	}

	@Override
	public void pathExtension(String extension) {

	}

	@Override
	public void header(String name, String value) {
		headers.add(name);
		headers.add(value);
	}

	@Override
	public void queryParam(String name, String value) {
	}

	@Override
	public void startAnd() {

	}

	@Override
	public void and() {

	}

	@Override
	public void endAnd() {

	}

	@Override
	public void startOr() {

	}

	@Override
	public void or() {

	}

	@Override
	public void endOr() {

	}

	@Override
	public void startNegate() {

	}

	@Override
	public void endNegate() {

	}

	@Override
	public void unknown(RequestPredicate predicate) {

	}
}
