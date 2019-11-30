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
package com.lessspring.org.utils;

import com.lessspring.org.context.TraceContextHolder;
import com.lessspring.org.model.vo.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.BiConsumer;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
public final class RenderUtils {

	private static TraceContextHolder contextHolder = TraceContextHolder.getInstance();

	public static Mono<ServerResponse> render(ResponseData data) {
		return render(Mono.justOrEmpty(data));
	}

	@SuppressWarnings("all")
	public static Mono<ServerResponse> render(Mono<?> dataMono) {
		return ok().header("Access-Control-Allow-Origin", "*")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.cacheControl(CacheControl.noCache())
				.body(BodyInserters.fromPublisher(dataMono, (Class) ResponseData.class))
				.doOnSuccessOrError(new BiConsumer<Mono<ServerResponse>, Throwable>() {
					@Override
					public void accept(Mono<ServerResponse> o, Throwable o2) {
						log.info("Trace Info : {}", contextHolder.getInvokeTraceContext());
					}
				});
	}

	@SuppressWarnings("all")
	public static Mono<ServerResponse> render(Resource resource) {
		return ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + resource.getFilename() + "\"")
				.body(BodyInserters.fromResource(resource))
				.doOnSuccessOrError(new BiConsumer<ServerResponse, Throwable>() {
					@Override
					public void accept(ServerResponse serverResponse, Throwable throwable) {
						log.info("Trace Info : {}", contextHolder.getInvokeTraceContext());
					}
				});
	}

}
