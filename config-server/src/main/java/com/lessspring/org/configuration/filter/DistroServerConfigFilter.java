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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.lessspring.org.raft.vo.ServerNode;
import com.lessspring.org.service.cluster.DistroRouter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

/**
 * Data fragmentation intercept processor
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component
public class DistroServerConfigFilter implements CustomerConfigFilter {

	private final WebClient client = WebClient.create();

	private final DistroRouter distroRouter = DistroRouter.getInstance();

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, FilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		ServerHttpResponse response = exchange.getResponse();
		String shareId = request.getQueryParams().getFirst("shareId");
		if (StringUtils.isEmpty(shareId)) {
			return filterResponse(response, HttpStatus.BAD_REQUEST, "Illegal request");
		}
		final HttpMethod method = Optional.ofNullable(request.getMethod())
				.orElse(HttpMethod.GET);
		final String reqPath = router(shareId) + request.getURI().getRawPath() + "?"
				+ request.getURI().getRawQuery();
		log.info("http request redirect : source {} => target {}",
				distroRouter.self() + "?" + request.getURI().getRawQuery(), reqPath);
		if (distroRouter.isPrincipal(shareId)) {
			return chain.filter(exchange);
		}
		// Forward requests to different nodes
		Mono<ClientResponse> clientResponseMono = client.method(method).uri(reqPath)
				.contentType(MediaType.APPLICATION_JSON_UTF8).headers(httpHeaders -> {
					HttpHeaders rawHeaders = request.getHeaders();
					for (Map.Entry<String, List<String>> entry : rawHeaders.entrySet()) {
						httpHeaders.addAll(entry.getKey(), entry.getValue());
					}
				}).body(request.getBody(), DataBuffer.class).exchange();
		return clientResponseMono
				.flatMap(clientResponse -> clientResponse.toEntity(String.class))
				.flatMap(entity -> filterResponse(response,
						entity.hasBody() ? "" : entity.getBody()));
	}

	private String router(String key) {
		ServerNode node = distroRouter.route(key);
		return "http://" + node.getKey();
	}

	@Override
	public int priority() {
		return HIGH_PRIORITY;
	}
}
