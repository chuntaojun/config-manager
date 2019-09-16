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
package com.lessspring.org.service.publish;

import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.model.vo.WatchRequest;
import com.lessspring.org.pojo.event.ConfigChangeEvent;
import com.lessspring.org.pojo.event.NotifyEvent;
import com.lessspring.org.utils.GsonUtils;
import com.lessspring.org.utils.ListenKeyUtils;
import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component
public class WatchClientManager implements EventHandler<NotifyEvent> {

    private Map<String, Set<WatchClient>> watchClientManager = new ConcurrentHashMap<>();

    public void createWatchClient(WatchRequest request, FluxSink sink, ServerRequest serverRequest) {
        WatchClient client = WatchClient.builder()
                .clientIp(Objects.requireNonNull(serverRequest.exchange().getRequest().getRemoteAddress()).getHostString())
                .checkKey(request.getWatchKey())
                .namespaceId(request.getNamespaceId())
                .response(serverRequest.exchange().getResponse())
                .build();
        Map<String, String> listenKeys = client.getCheckKey();
        listenKeys.forEach((key, value) -> {
            Set<WatchClient> clients = watchClientManager.computeIfAbsent(key, s -> new HashSet<WatchClient>());
            synchronized (clients) {
                clients.add(client);
            }
        });
    }

    @Override
    public void onEvent(NotifyEvent event, long sequence, boolean endOfBatch) throws Exception {
        String listenKey = ListenKeyUtils.buildListenKey(event.getNamespaceId(), event.getDataId(), event.getGroupId());
        long[] finishWorks = new long[1];
        long works = watchClientManager.getOrDefault(listenKey, Collections.emptySet())
                .stream()
                .peek(client -> {
                    try {
//                        writeResponse(client, data);
                        finishWorks[0] ++;
                    } catch (Exception e) {
                        log.error("[Notify WatchClient has Error] : {}", e.getMessage());
                    }
                })
                .count();
        log.info("total notify clients number is : {}, finish success is : {}", works, finishWorks[0]);
    }

    private void writeResponse(WatchClient client, ResponseData data) {
        ServerHttpResponse response = client.getResponse();
        client.getSink().next(response.writeWith(Mono.just(response.bufferFactory()
                .wrap(GsonUtils.toJson(data).getBytes(StandardCharsets.UTF_8)))));
    }

}
