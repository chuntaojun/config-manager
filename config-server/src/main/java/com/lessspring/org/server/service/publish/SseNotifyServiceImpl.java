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

import com.lessspring.org.constant.StringConst;
import com.lessspring.org.constant.WatchType;
import com.lessspring.org.model.vo.WatchRequest;
import com.lessspring.org.server.pojo.vo.WatchClientVO;
import com.lessspring.org.server.service.publish.client.SseWatchClient;
import com.lessspring.org.server.service.publish.client.WatchClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.FluxSink;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component
public class SseNotifyServiceImpl extends AbstractNotifyServiceImpl {

	// Build with the Client corresponds to a monitored object is
	// used to monitor configuration changes

	public void createWatchClient(WatchRequest request, FluxSink<?> sink,
			ServerRequest serverRequest) {
		WatchClient client = SseWatchClient.builder()
				.clientId(serverRequest.headers().asHttpHeaders()
						.getFirst(StringConst.CLIENT_ID_NAME))
				.clientIp(Objects
						.requireNonNull(
								serverRequest.exchange().getRequest().getRemoteAddress())
						.getHostString())
				.checkKey(request.getWatchKey()).namespaceId(request.getNamespaceId())
				.response(serverRequest.exchange().getResponse()).sink(sink).build();
		// When event creation is cancelled, automatic cancellation of client
		// on the server side corresponding to monitor object
		sink.onDispose(() -> {
			client.onClose();
			synchronized (monitor) {
				clientCnt--;
			}
			// For in the form of the key : NameUtils.buildName(groupId, dataId)
			Map<String, Set<WatchClient>> namespaceWatcher = watchClientManager
					.getOrDefault(client.getNamespaceId(), Collections.emptyMap());
			for (Map.Entry<String, Set<WatchClient>> entry : namespaceWatcher
					.entrySet()) {
				entry.getValue().remove(client);
			}
		});
		createWatchClient(client);
	}

	// Send the event to the client

	@Override
	@SuppressWarnings("unchecked")
	protected void writeResponse(WatchClient client, Object data) {
		((SseWatchClient) client).getSink().next(data);
	}

	@Override
	protected boolean compareConfigSign(String oldSign, String newSign) {
		return true;
	}

	@Override
	public List<WatchClientVO> queryWatchClient(String namespaceId, String groupId,
			String dataId) {
		List<WatchClientVO> target = super.queryWatchClient(namespaceId, groupId, dataId);
		for (WatchClientVO client : target) {
			client.setWatchType(WatchType.SSE.name());
		}
		return target;
	}

}
