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
package com.lessspring.org.server.configuration.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.lessspring.org.constant.StringConst;
import com.lessspring.org.context.TraceContext;
import com.lessspring.org.context.TraceContextHolder;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.server.metrics.MetricsHelper;
import com.lessspring.org.server.pojo.Privilege;
import com.lessspring.org.server.service.security.SecurityService;
import com.lessspring.org.server.utils.GsonUtils;
import io.micrometer.core.instrument.DistributionSummary;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static com.lessspring.org.server.utils.PropertiesEnum.Hint.HASH_NO_PRIVILEGE;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Configuration
public class ConfigWebFilter implements WebFilter {

	private final FilterChain filterChain;
	private final SecurityService securityService;
	private TraceContextHolder contextHolder = TraceContextHolder.getInstance();
	@Value("${com.lessspring.org.config-manager.anyuri:/}")
	private String[] anyOneUri;
	private DistributionSummary httpTrace;

	public ConfigWebFilter(SecurityService securityService, FilterChain filterChain) {
		this.securityService = securityService;
		this.filterChain = filterChain;
	}

	@PostConstruct
	public void init() {
		httpTrace = MetricsHelper.builderSummary("conf-http-trace", "conf-http-trace");
		filterChain.init(anyOneUri);
	}

	@PreDestroy
	public void destroy() {
		filterChain.destroy();
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		httpTrace.record(1.0D);
		ServerHttpRequest request = exchange.getRequest();
		String path = request.getPath().value();
		// Filter ahead of time by URL whitelist
		if (uriMatcher(path, anyOneUri)) {
			return chain.filter(exchange);
		}
		// Give priority to perform user custom interceptors
		Mono<Void> mono = filterChain.filter(exchange);
		filterChain.destroy();
		if (Objects.nonNull(mono)) {
			return mono;
		}
		// To release the URL white list
		openTraceContext(request);
		boolean hasAuth = permissionIntercept(exchange);
		if (!hasAuth) {
			return filterResponse(exchange.getResponse(), HttpStatus.UNAUTHORIZED,
					HASH_NO_PRIVILEGE.getDescribe());
		}
		Mono<Void> result = chain.filter(exchange);
		httpTrace.record(-1.0D);
		return result;
	}

	private void openTraceContext(ServerHttpRequest request) {
		String traceInfo = request.getHeaders().getFirst("c-trace-info");
		final TraceContext context;
		if (StringUtils.isNotBlank(traceInfo)) {
			context = GsonUtils.toObj(traceInfo, TraceContext.class);
		}
		else {
			context = new TraceContext();
		}
		contextHolder.setInvokeTraceContext(context);
	}

	private boolean permissionIntercept(ServerWebExchange exchange) {
		ServerHttpRequest request = exchange.getRequest();
		String token = request.getHeaders().getFirst(StringConst.TOKEN_HEADER_NAME);
		if (StringUtils.isEmpty(token)) {
			return false;
		}
		if (securityService.isExpire(token)) {
			return false;
		}
		Optional<DecodedJWT> result = securityService.verify(token);
		result.ifPresent(decodedJWT -> exchange.getSession().subscribe(webSession -> {
			Privilege privilege = GsonUtils.toObj(decodedJWT.getSubject(),
					Privilege.class);
			TraceContext context = contextHolder.getInvokeTraceContext();
			context.setAttachment("privilege", privilege);
			webSession.getAttributes().put("privilege", privilege);
		}));
		return result.isPresent();
	}

	private boolean uriMatcher(String path, String[] matcherUri) {
		return Arrays.stream(matcherUri).anyMatch(path::startsWith);
	}

	private Mono<Void> filterResponse(ServerHttpResponse response, HttpStatus status,
			String s) {
		return filterResponse(response, status.value(), s);
	}

	@SuppressWarnings("unchecked")
	private Mono<Void> filterResponse(ServerHttpResponse response, int code, String s) {
		return response
				.writeWith(Mono.just(response.bufferFactory().wrap(GsonUtils.toJsonBytes(
						ResponseData.builder().withCode(code).withData(s).build()))));
	}

}
