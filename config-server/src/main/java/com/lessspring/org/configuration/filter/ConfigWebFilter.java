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

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.lessspring.org.constant.StringConst;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.pojo.Privilege;
import com.lessspring.org.service.security.SecurityService;
import com.lessspring.org.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import static com.lessspring.org.utils.PropertiesEnum.Hint.HASH_NO_PRIVILEGE;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Configuration
public class ConfigWebFilter implements WebFilter {

	@Value("${com.lessspring.org.config-manager.anyuri}")
	private String[] anyOneUri;

	private final FilterChain filterChain = new DefaultFilterChain();

	private final SecurityService securityService;

	public ConfigWebFilter(SecurityService securityService) {
		this.securityService = securityService;
	}

	@PostConstruct
	public void init() {
		filterChain.init();
	}

	@PreDestroy
	public void destroy() {
		filterChain.destroy();
	}

	@NotNull
	@Override
	public Mono<Void> filter(@NotNull ServerWebExchange exchange,
			@NotNull WebFilterChain chain) {
		// Give priority to perform user custom interceptors
		Mono<Void> mono = filterChain.filter(exchange);
		filterChain.destroy();
		if (Objects.nonNull(mono)) {
			return mono;
		}
		boolean hasAuth = permissionIntercept(exchange);
		if (!hasAuth) {
			return filterResponse(exchange.getResponse(), HttpStatus.UNAUTHORIZED,
					HASH_NO_PRIVILEGE.getDescribe());
		}
		return chain.filter(exchange);
	}

	boolean permissionIntercept(ServerWebExchange exchange) {
		ServerHttpRequest request = exchange.getRequest();
		String path = request.getPath().value();
		if (uriMatcher(path, anyOneUri)) {
			return true;
		}
		String token = request.getHeaders().getFirst(StringConst.TOKEN_HEADER_NAME);
		if (StringUtils.isEmpty(token)) {
			return false;
		}
		if (securityService.isExpire(token)) {
			return false;
		}
		Optional<DecodedJWT> result = securityService.verify(token);
		result.ifPresent(decodedJWT -> {
			exchange.getSession().subscribe(webSession -> {
				Privilege privilege = GsonUtils.toObj(decodedJWT.getSubject(),
						Privilege.class);
				webSession.getAttributes().put("privilege", privilege);
			});
		});
		return result.isPresent();
	}

	private boolean uriMatcher(String path, String[] matcherUri) {
		return Arrays.stream(matcherUri).anyMatch(path::startsWith);
	}

	@SuppressWarnings("unchecked")
	private Mono<Void> filterResponse(ServerHttpResponse response, HttpStatus status,
			String s) {
		return response.writeWith(
				Mono.just(response.bufferFactory().wrap(GsonUtils.toJsonBytes(ResponseData
						.builder().withCode(status.value()).withData(s).build()))));
	}

}
