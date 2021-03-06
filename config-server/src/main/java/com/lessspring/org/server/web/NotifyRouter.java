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
package com.lessspring.org.server.web;

import com.lessspring.org.constant.StringConst;
import com.lessspring.org.server.handler.NotifyHandler;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Configuration
@SuppressWarnings("all")
public class NotifyRouter extends BaseRouter {

	private final NotifyHandler notifyHandler;

	public NotifyRouter(NotifyHandler notifyHandler) {
		this.notifyHandler = notifyHandler;
	}

	@Bean(value = "notifyRouterImpl")
	public RouterFunction<ServerResponse> notifyRouter() {

		Tuple2<RequestPredicate, HandlerFunction> watchSse = Tuples.of(
				POST(StringConst.API_V1 + "watch/sse")
						.and(contentType(MediaType.APPLICATION_JSON_UTF8)),
				new HandlerFunction() {
					@Override
					public Mono handle(ServerRequest request) {
						return notifyHandler.watchSse(request);
					}
				});

		Tuple2<RequestPredicate, HandlerFunction> watchLp = Tuples.of(
				POST(StringConst.API_V1 + "watch/longPoll")
						.and(contentType(MediaType.APPLICATION_JSON_UTF8)),
				new HandlerFunction() {
					@Override
					public Mono handle(ServerRequest request) {
						return notifyHandler.watchLongPoll(request);
					}
				});

		Tuple2<RequestPredicate, HandlerFunction> queryClient = Tuples
				.of(GET(StringConst.API_V1 + "config/watchClient").and(
						accept(MediaType.APPLICATION_JSON_UTF8)), new HandlerFunction() {
							@Override
							public Mono handle(ServerRequest request) {
								return notifyHandler.watchClients(request);
							}
						});

		registerVisitor(watchSse, watchLp, queryClient);

		RouterFunction<ServerResponse> function = route(watchSse.getT1(),
				watchSse.getT2()).andRoute(watchLp.getT1(), watchLp.getT2())
						.andRoute(queryClient.getT1(), queryClient.getT2());
		return function;
	}

}
