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
package com.lessspring.org.configuration;

import java.util.Map;

import javax.annotation.PostConstruct;

import com.lessspring.org.exception.BaseException;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.utils.RenderUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Configuration
public class GlobalErrorWebExceptionConfiguration {

	@Component
	@Order(-2)
	public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

		public GlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes,
				ResourceProperties resourceProperties,
				ApplicationContext applicationContext,
				ServerCodecConfigurer serverCodecConfigurer) {
			super(errorAttributes, resourceProperties, applicationContext);
			super.setMessageWriters(serverCodecConfigurer.getWriters());
			super.setMessageReaders(serverCodecConfigurer.getReaders());
		}

		@PostConstruct
		public void init() {
		}

		@Override
		protected RouterFunction<ServerResponse> getRoutingFunction(
				ErrorAttributes errorAttributes) {
			return RouterFunctions.route(RequestPredicates.all(),
					this::renderErrorResponse);
		}

		@SuppressWarnings("unchecked")
		private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
			final Map<String, Object> errorMap = getErrorAttributes(request, true);
			Throwable throwable = getError(request);
			log.error("[The request url information]：{}", request.uri());
			log.error("[Internal error information]：{}",
					String.valueOf(errorMap.get("trace")).substring(0, 2_000));
			Mono<ResponseData> errMono;
			if (throwable instanceof BaseException) {
				BaseException exception = (BaseException) throwable;
				errMono = Mono
						.just(ResponseData.builder().withCode(exception.code().getCode())
								.withErrMsg(exception.getMessage())
								.withData(exception.code()).build());
			}
			else {
				errMono = Mono.just(ResponseData.builder()
						.withCode(
								Integer.parseInt(String.valueOf(errorMap.get("status"))))
						.withErrMsg("Inner Error").withData(errorMap.get("trace"))
						.build());
			}
			return RenderUtils.render(errMono);
		}

	}

}
