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
package com.lessspring.org.handler.impl;

import com.lessspring.org.handler.NotifyHandler;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.model.vo.WatchRequest;
import com.lessspring.org.service.publish.WatchClientManager;
import com.lessspring.org.utils.SseUtils;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

import static org.springframework.web.reactive.function.BodyInserters.fromServerSentEvents;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Service(value = "notifyHandler")
public class NotifyHandlerImpl implements NotifyHandler {

    private final WatchClientManager watchClientManager;

    public NotifyHandlerImpl(WatchClientManager watchClientManager) {
        this.watchClientManager = watchClientManager;
    }

    @NotNull
    @Override
    public Mono<ServerResponse> watch(ServerRequest request) {
        return request.bodyToMono(WatchRequest.class)
                .map(watchRequest -> Flux.create(fluxSink -> watchClientManager.createWatchClient(watchRequest, fluxSink, request)))
                .flatMap(objectFlux -> {
                    return ok().contentType(MediaType.TEXT_EVENT_STREAM)
                            .body(objectFlux.map(o -> {
                                return SseUtils.createServerSentEvent(ResponseData.success(o));
                            }), ServerSentEvent.class);
                });
    }

}
