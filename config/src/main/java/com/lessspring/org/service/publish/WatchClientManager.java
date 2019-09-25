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

import com.lessspring.org.DiskUtils;
import com.lessspring.org.NameUtils;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.model.vo.WatchRequest;
import com.lessspring.org.pojo.event.ConfigChangeEvent;
import com.lessspring.org.pojo.event.NotifyEvent;
import com.lessspring.org.utils.GsonUtils;
import com.lessspring.org.utils.ListenKeyUtils;
import com.lessspring.org.utils.MD5Utils;
import com.lessspring.org.utils.SseUtils;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.Disposable;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component
public class WatchClientManager implements WorkHandler<NotifyEvent> {

    private Map<String, Map<String, Set<WatchClient>>> watchClientManager = new ConcurrentHashMap<>();

    public void createWatchClient(WatchRequest request, FluxSink<?> sink, ServerRequest serverRequest) {
        WatchClient client = WatchClient.builder()
                .clientIp(Objects.requireNonNull(serverRequest.exchange().getRequest().getRemoteAddress()).getHostString())
                .checkKey(request.getWatchKey())
                .namespaceId(request.getNamespaceId())
                .response(serverRequest.exchange().getResponse())
                .sink(sink)
                .build();
        sink.onDispose(() -> {
            Map<String, Set<WatchClient>> namespaceWatcher = watchClientManager.getOrDefault(client.getNamespaceId(), Collections.emptyMap());
            for (Map.Entry<String, Set<WatchClient>> entry : namespaceWatcher.entrySet()) {
                entry.getValue().remove(client);
            }
        });
        Map<String, String> listenKeys = client.getCheckKey();
        listenKeys.forEach((key, value) -> {
            Map<String, Set<WatchClient>> clientsMap = watchClientManager.computeIfAbsent(client.getNamespaceId(), s -> new ConcurrentHashMap<>());
            clientsMap.computeIfAbsent(key, s -> new CopyOnWriteArraySet<>());
            clientsMap.get(key).add(client);
        });
        doQuickCompare(client);
    }

    private void doQuickCompare(WatchClient watchClient) {
    }

    @SuppressWarnings("unchecked")
    private void writeResponse(WatchClient client, Object data) {
        client.getSink().next(data);
    }

    @Override
    public void onEvent(NotifyEvent event) throws Exception {
        try {
            final String key = NameUtils.buildName(event.getGroupId(), event.getDataId());
            final String configInfoJson = DiskUtils.readFile(event.getNamespaceId(), key);
            long[] finishWorks = new long[1];
            watchClientManager.getOrDefault(event.getNamespaceId(), Collections.emptyMap())
                    .entrySet()
                    .stream()
                    .flatMap(stringSetEntry -> stringSetEntry.getValue().stream())
                    .forEach(client -> {
                        try {
                            writeResponse(client, configInfoJson);
                            finishWorks[0]++;
                        } catch (Exception e) {
                            log.error("[Notify WatchClient has Error] : {}", e.getMessage());
                        }
                    });
            log.info("total notify clients finish success is : {}", finishWorks[0]);
        } catch (Exception e) {
            log.error("notify watcher has some error : {}", e);
        }
    }

}
