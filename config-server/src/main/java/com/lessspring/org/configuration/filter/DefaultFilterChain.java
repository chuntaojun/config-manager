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
package com.lessspring.org.configuration.filter;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.ServiceLoader;

import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Component
class DefaultFilterChain implements FilterChain {

	private final LinkedList<CustomerConfigFilter> filters = new LinkedList<>();

	private ThreadLocal<LinkedList<CustomerConfigFilter>> filterLocal;

	DefaultFilterChain(@Autowired DistroServerConfigFilter configFilter) {
		ServiceLoader<CustomerConfigFilter> loader = ServiceLoader
				.load(CustomerConfigFilter.class);
		for (CustomerConfigFilter filter : loader) {
			filters.add(filter);
		}
		filters.sort(Comparator.comparingInt(CustomerConfigFilter::priority));
		filters.addFirst(configFilter);
	}

	@Override
	public void init() {
		// Each thread a copy interceptors
		filterLocal = ThreadLocal.withInitial(() -> new LinkedList<>(filters));
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange) {
		final LinkedList<CustomerConfigFilter> filters = filterLocal.get();
		if (filters.isEmpty()) {
			return null;
		}
		CustomerConfigFilter filter = filters.pollFirst();
		return filter.filter(exchange, this);
	}

	@Override
	public void destroy() {
		filterLocal.remove();
	}
}
