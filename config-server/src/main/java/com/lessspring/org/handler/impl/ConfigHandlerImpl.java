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

import com.lessspring.org.configuration.security.NeedAuth;
import com.lessspring.org.handler.ConfigHandler;
import com.lessspring.org.model.vo.DeleteConfigRequest;
import com.lessspring.org.model.vo.PublishConfigRequest;
import com.lessspring.org.model.vo.QueryConfigRequest;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.service.config.ConfigOperationService;
import com.lessspring.org.service.config.OperationService;
import com.lessspring.org.tps.LimitRule;
import com.lessspring.org.tps.OpenTpsLimit;
import com.lessspring.org.utils.ReactiveWebUtils;
import com.lessspring.org.utils.RenderUtils;
import com.lessspring.org.utils.SchedulerUtils;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@OpenTpsLimit
@Service(value = "configHandler")
public class ConfigHandlerImpl implements ConfigHandler {

	private final OperationService operationService;

	public ConfigHandlerImpl(OperationService operationService) {
		this.operationService = operationService;
	}

	@NotNull
	@Override
	@LimitRule(resource = "publish-config", qps = 500)
	@NeedAuth(argueName = "namespaceId")
	public Mono<ServerResponse> publishConfig(ServerRequest request) {
		final String namespaceId = request.queryParam("namespaceId").orElse("default");
		return request.bodyToMono(PublishConfigRequest.class)
				.publishOn(Schedulers
						.fromExecutor(SchedulerUtils.getSingleton().WEB_HANDLER))
				.map(publishRequest -> operationService.publishConfig(namespaceId,
						publishRequest))
				.map(Mono::just).flatMap(RenderUtils::render);
	}

	@NotNull
	@Override
	@LimitRule(resource = "publish-config", qps = 500)
	@NeedAuth(argueName = "namespaceId")
	public Mono<ServerResponse> modifyConfig(ServerRequest request) {
		final String namespaceId = request.queryParam("namespaceId").orElse("default");
		return request.bodyToMono(PublishConfigRequest.class)
				.publishOn(Schedulers
						.fromExecutor(SchedulerUtils.getSingleton().WEB_HANDLER))
				.map(publishRequest -> operationService.modifyConfig(namespaceId,
						publishRequest))
				.map(Mono::just).flatMap(RenderUtils::render);
	}

	@NotNull
	@Override
	@LimitRule(resource = "query-config", qps = 2000)
	@NeedAuth(argueName = "namespaceId")
	public Mono<ServerResponse> queryConfig(ServerRequest request) {
		final String namespaceId = request.queryParam("namespaceId").orElse("default");
		final String groupId = request.queryParam("groupId").orElse("DEFAULT_GROUP");
		final String dataId = request.queryParam("dataId").orElse("");
		final QueryConfigRequest queryRequest = QueryConfigRequest.sbuilder()
				.groupId(groupId).dataId(dataId).build();
		final String clientIp = request.remoteAddress().orElse(ReactiveWebUtils.ALL_IP)
				.getHostName();
		queryRequest.setAttribute("clientIp", clientIp);
		Mono<ResponseData<?>> mono = Mono
				.just(operationService.queryConfig(namespaceId, queryRequest));
		mono = mono.publishOn(
				Schedulers.fromExecutor(SchedulerUtils.getSingleton().WEB_HANDLER));
		return RenderUtils.render(mono);
	}

	@NotNull
	@Override
	@LimitRule(resource = "publish-config", qps = 500)
	@NeedAuth(argueName = "namespaceId")
	public Mono<ServerResponse> removeConfig(ServerRequest request) {
		final String namespaceId = request.queryParam("namespaceId").orElse("default");
		final String groupId = request.queryParam("groupId").orElse("DEFAULT_GROUP");
		final String dataId = request.queryParam("dataId").orElse("");
		final boolean isBeta = Boolean
				.parseBoolean(request.queryParam("beta").orElse("false"));
		final DeleteConfigRequest deleteRequest = DeleteConfigRequest.sbuilder()
				.beta(isBeta).groupId(groupId).dataId(dataId).build();
		Mono<ResponseData<?>> mono = Mono
				.just(operationService.removeConfig(namespaceId, deleteRequest));
		mono = mono.publishOn(
				Schedulers.fromExecutor(SchedulerUtils.getSingleton().WEB_HANDLER));
		return RenderUtils.render(mono);
	}
}
