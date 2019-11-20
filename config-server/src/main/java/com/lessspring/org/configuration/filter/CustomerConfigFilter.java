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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.lessspring.org.Priority;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.utils.GsonUtils;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public interface CustomerConfigFilter extends Priority {

	/**
	 * The interceptor
	 *
	 * @param exchange {@link ServerWebExchange}
	 * @param chain {@link FilterChain}
	 * @return {@link Mono<Void>}
	 */
	Mono<Void> filter(ServerWebExchange exchange, FilterChain chain);

	/**
	 * Invoke this method returns a response
	 *
	 * @param response {@link ServerHttpResponse}
	 * @param status {@link HttpStatus}
	 * @param s response msg
	 * @return {@link Mono<Void>}
	 */
	@SuppressWarnings("unchecked")
	default Mono<Void> filterResponse(ServerHttpResponse response, HttpStatus status,
			String s) {
		return response.writeWith(
				Mono.just(response.bufferFactory().wrap(GsonUtils.toJsonBytes(ResponseData
						.<String>builder().withCode(status.value()).withData(s).build()))));
	}

	/**
	 * Invoke this method returns a response
	 *
	 * @param response {@link ServerHttpResponse}
	 * @param s response msg
	 * @return {@link Mono<Void>}
	 */
	default Mono<Void> filterResponse(ServerHttpResponse response, String s) {
		return response.writeWith(Mono.just(response.bufferFactory()
				.wrap(s.getBytes(Charset.forName(StandardCharsets.UTF_8.name())))));
	}

}
