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

package com.lessspring.org.server.service.publish;

import com.lessspring.org.StrackTracekUtils;
import com.lessspring.org.model.vo.WatchRequest;
import com.lessspring.org.server.service.publish.client.LpWatchClient;
import com.lessspring.org.server.service.publish.client.WatchClient;
import com.lessspring.org.server.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 * @Created at 2019/12/12 12:26 上午
 */
@Slf4j
@Service
public class LongPollNotifyServiceImpl extends AbstractNotifyServiceImpl {

	@SuppressWarnings("unchecked")
	public void createWatchClient(WatchRequest request, ServerRequest serverRequest,
			MonoSink monoSink, Mono mono) {
		String s = serverRequest.headers().asHttpHeaders().getFirst("hold-time");
		if (StringUtils.isEmpty(s)) {
			monoSink.success("Invalid long polling request: please set hold-time");
			return;
		}
		long holdTimeOut = Long.parseLong(s);
		WatchClient client = LpWatchClient.builder()
				.clientIp(Objects
						.requireNonNull(
								serverRequest.exchange().getRequest().getRemoteAddress())
						.getHostString())
				.checkKey(request.getWatchKey()).namespaceId(request.getNamespaceId())
				.holdTime(holdTimeOut).response(serverRequest.exchange().getResponse())
				.build();
		createWatchClient(client);
		mono.timeout(Duration.ofSeconds(holdTimeOut - 1)).subscribe(o -> {
		}, throwable -> {
			if (throwable instanceof TimeoutException) {
				doQuickCompare(client);
				return;
			}
			log.error("error : {}", StrackTracekUtils.stackTrace((Throwable) throwable));
			monoSink.success("");
		});
	}

	@Override
	protected void writeResponse(WatchClient client, Object data) {
		LpWatchClient watchClient = (LpWatchClient) client;
		if (watchClient.isActive()) {
			watchClient.send(GsonUtils.toJson(data));
			watchClient.shutdown();
			Map<String, Set<WatchClient>> clients = watchClientManager
					.get(client.getNamespaceId());
			for (Map.Entry<String, Set<WatchClient>> entry : clients.entrySet()) {
				entry.getValue().remove(client);
			}
		}
		else {
			log.warn("[LongPollNotifyServiceImpl] This polling task for the client ends");
		}
	}

	@Override
	protected boolean compareConfigSign(String oldSign, String newSign) {
		return false;
	}

}
