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
package com.lessspring.org.server.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Supplier;

import com.lessspring.org.InetUtils;
import com.lessspring.org.model.vo.ResponseData;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.server.ServerRequest;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class ReactiveWebUtils {

	public static Optional<Object> getAttribute(String name, ServerRequest request) {
		return request.attribute(name);
	}

	private static Supplier<String> getSelfIp() {
		return InetUtils::getSelfIp;
	}

	public static Mono<Void> filterResponse(ServerHttpResponse response, String s) {
		return response.writeWith(Mono.just(response.bufferFactory()
				.wrap(s.getBytes(Charset.forName(StandardCharsets.UTF_8.name())))));
	}

	@SuppressWarnings("unchecked")
	public static Mono<Void> filterResponse(ServerHttpResponse response,
			HttpStatus status, String s) {
		return response.writeWith(Mono.just(response.bufferFactory()
				.wrap(GsonUtils.toJsonBytes(ResponseData.<String> builder()
						.withCode(status.value()).withData(s).build()))));
	}

}
