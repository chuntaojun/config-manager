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

import com.lessspring.org.PathUtils;
import com.lessspring.org.jvm.JvmUtils;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.server.exception.BaseException;
import com.lessspring.org.server.metrics.MetricsHelper;
import com.lessspring.org.server.pojo.event.email.ErrorEmailEvent;
import com.lessspring.org.server.service.common.EmailService;
import com.lessspring.org.server.utils.PropertiesEnum;
import com.lessspring.org.server.utils.RenderUtils;
import io.micrometer.core.instrument.DistributionSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.io.File;
import java.time.LocalDate;
import java.util.Map;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Configuration
public class GlobalErrorWebExceptionConfiguration {

	@Autowired
	private EmailService emailService;

	private DistributionSummary summary;

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
			summary = MetricsHelper.builderSummary("totalError",
					"Record the number of errors during run time");
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
			String errMsg = String.valueOf(errorMap.get("trace"));
			log.error("[The request url information]：{}", request.uri());
			log.error("[Internal error information]：{}",
					errMsg.length() > 2_000 ? errMsg.substring(0, 2_000) : errMsg);
			Mono<ResponseData<?>> errMono;
			if (throwable instanceof OutOfMemoryError) {
				log.error("Emergency error : OutOfMemoryError");
				final String fileName = PathUtils.finalPath("jvm",
						"config-manager-jvm-" + LocalDate.now());
				log.info("[Dump Jvm File] : file name : {}", fileName);
				final File file;
				try {
					final ErrorEmailEvent emailEvent = new ErrorEmailEvent(
							PropertiesEnum.EmailType.ERROR);
					file = JvmUtils.jMap(fileName, true);
					emailEvent.setTitle("[Application has OutOfMemoryError]");
					emailEvent.setAttachment(file);
					emailEvent.setMsg(throwable.getLocalizedMessage());
					emailService.publishEmailEvent(emailEvent);
				}
				catch (Exception e) {
					throwable = e;
				}
				errMono = Mono.just(ResponseData.fail(throwable));
			}
			else if (throwable instanceof BaseException) {
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
			summary.record(1.0D);
			return RenderUtils.render(errMono);
		}

	}

}
