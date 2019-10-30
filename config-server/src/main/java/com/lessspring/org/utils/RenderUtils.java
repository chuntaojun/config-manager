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

import com.lessspring.org.model.vo.ResponseData;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class RenderUtils {

	@SuppressWarnings("unchecked")
	public static Mono<ServerResponse> render(Mono<?> dataMono) {
		return ok().header("Access-Control-Allow-Origin", "*")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.cacheControl(CacheControl.noCache())
				.body(BodyInserters.fromPublisher(dataMono, (Class) ResponseData.class))
				.subscribeOn(Schedulers.fromExecutor(SchedulerUtils.WEB_HANDLER));
	}

}
