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
package com.lessspring.org.server.handler.impl;

import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.model.vo.WatchRequest;
import com.lessspring.org.server.handler.NotifyHandler;
import com.lessspring.org.server.pojo.request.WatchClientQueryPage;
import com.lessspring.org.server.pojo.vo.WatchClientVO;
import com.lessspring.org.server.service.publish.LongPollNotifyServiceImpl;
import com.lessspring.org.server.service.publish.SseNotifyServiceImpl;
import com.lessspring.org.server.utils.RenderUtils;
import com.lessspring.org.server.utils.SseUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * 长轮询监听接口
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Service(value = "notifyHandler")
public class NotifyHandlerImpl implements NotifyHandler {

	private final SseNotifyServiceImpl sseNotifyService;
	private final LongPollNotifyServiceImpl longPollNotifyService;

	public NotifyHandlerImpl(SseNotifyServiceImpl sseNotifyServiceImpl,
			LongPollNotifyServiceImpl longPollNotifyService) {
		this.sseNotifyService = sseNotifyServiceImpl;
		this.longPollNotifyService = longPollNotifyService;
	}

	// the SSE push FluxSink, enables the Server end to independently
	// choose the timing of the push and directional push

	@Override
	public Mono<ServerResponse> watchSse(ServerRequest request) {
		return request.bodyToMono(WatchRequest.class)
				.map(watchRequest -> Flux.create(fluxSink -> sseNotifyService
						.createWatchClient(watchRequest, fluxSink, request)))
				.flatMap(objectFlux -> {
					return ok().contentType(MediaType.TEXT_EVENT_STREAM)
							.body(objectFlux.map(o -> {
								return SseUtils
										.createServerSentEvent(ResponseData.success(o));
							}), ServerSentEvent.class);
				});
	}

	// the long-poll push MonoSink, enables the Server end to independently
	// choose the timing of the push and directional push

	@Override
	public Mono<ServerResponse> watchLongPoll(ServerRequest request) {
		MonoSink[] sink = new MonoSink[] { null };
		return request.bodyToMono(WatchRequest.class).map(watchRequest -> {
			Mono mono = Mono.create(monoSink -> sink[0] = monoSink);
			longPollNotifyService.createWatchClient(watchRequest, request, sink[0], mono);
			return mono;
		}).flatMap(mono -> ok().contentType(MediaType.APPLICATION_JSON_UTF8)
				.body(mono.map(ResponseData::success), ResponseData.class));
	}

	@Override
	public Mono<ServerResponse> watchClients(ServerRequest request) {
		final String namespaceId = request.queryParam("namespaceId").orElse("default");
		final String groupId = request.queryParam("groupId").orElse("DEFAULT_GROUP");
		final String dataId = request.queryParam("dataId").get();
		if (StringUtils.isEmpty(dataId)) {
			return RenderUtils.render(ResponseData.fail("Illegal query param"));
		}
		final WatchClientQueryPage queryPage = WatchClientQueryPage.builder()
				.namespaceId(namespaceId)
				.groupId(groupId)
				.dataId(dataId)
				.build();
		List<WatchClientVO> lps = longPollNotifyService.queryWatchClient(queryPage);
		List<WatchClientVO> sses = sseNotifyService.queryWatchClient(queryPage);
		List<WatchClientVO> clients = new ArrayList<>();
		clients.addAll(lps);
		clients.addAll(sses);
		return RenderUtils.render(ResponseData.success(clients));
	}

}
