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

import com.lessspring.org.exception.AuthForbidException;
import com.lessspring.org.exception.BaseException;
import com.lessspring.org.model.vo.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

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
        protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
            return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
        }

        @SuppressWarnings("unchecked")
        private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
            final Map<String, Object> errorMap = getErrorAttributes(request, true);
            Throwable throwable = getError(request);
            log.error("[请求url信息]：{}", request.uri());
            log.error("[内部错误信息]：{}", errorMap.get("trace"));
            Mono<ResponseData> errMono;
            if (throwable instanceof BaseException) {
                BaseException exception = (BaseException) throwable;
                errMono = Mono.just(ResponseData.builder()
                        .withCode(exception.code().getCode())
                        .withErrMsg("Inner Error").withData(exception.code()).build());
            } else {
                errMono = Mono.just(ResponseData.builder()
                        .withCode(Integer.parseInt(String.valueOf(errorMap.get("status"))))
                        .withErrMsg("Inner Error").withData(errorMap.get("trace")).build());
            }
            return ServerResponse.ok().body(BodyInserters
                    .fromPublisher(errMono.publishOn(Schedulers.elastic()), ResponseData.class))
                    .subscribeOn(Schedulers.elastic());
        }

    }

}
