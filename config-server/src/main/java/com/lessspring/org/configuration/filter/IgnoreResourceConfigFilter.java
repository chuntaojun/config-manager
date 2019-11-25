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

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019-11-24 22:32
 */
public class IgnoreResourceConfigFilter implements CustomerConfigFilter {

	private String ignore = "/favicon.ico";

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, FilterChain chain) {
		final String url = exchange.getRequest().getPath().contextPath().value();
		if (url.contains(ignore)) {
			return filterResponse(exchange.getResponse(), "");
		}
		return null;
	}

	@Override
	public int priority() {
		return 0;
	}
}