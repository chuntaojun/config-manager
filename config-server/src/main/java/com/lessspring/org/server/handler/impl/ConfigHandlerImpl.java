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

import com.lessspring.org.constant.StringConst;
import com.lessspring.org.model.vo.DeleteConfigRequest;
import com.lessspring.org.model.vo.PublishConfigRequest;
import com.lessspring.org.model.vo.QueryConfigRequest;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.server.configuration.security.NeedAuth;
import com.lessspring.org.server.configuration.tps.LimitRule;
import com.lessspring.org.server.configuration.tps.OpenTpsLimit;
import com.lessspring.org.server.handler.ConfigHandler;
import com.lessspring.org.server.pojo.vo.ConfigDetailVO;
import com.lessspring.org.server.pojo.vo.ConfigListVO;
import com.lessspring.org.server.service.config.OperationService;
import com.lessspring.org.server.utils.PropertiesEnum;
import com.lessspring.org.server.utils.RenderUtils;
import com.lessspring.org.server.utils.SchedulerUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * 配置管理接口
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@OpenTpsLimit
@Service(value = "configHandler")
public class ConfigHandlerImpl implements ConfigHandler {

	private final OperationService operationService;

	public ConfigHandlerImpl(OperationService operationService) {
		this.operationService = operationService;
	}

	@Override
	@LimitRule(resource = "publish-config", qps = 500)
	@NeedAuth(argueName = "namespaceId", role = PropertiesEnum.Role.DEVELOPER)
	public Mono<ServerResponse> publishConfig(ServerRequest request) {
		final String namespaceId = request.queryParam("namespaceId").orElse("default");
		return request.bodyToMono(PublishConfigRequest.class)
				.publishOn(Schedulers
						.fromExecutor(SchedulerUtils.getSingleton().WEB_HANDLER))
				.map(publishRequest -> operationService.publishConfig(namespaceId,
						publishRequest))
				.map(Mono::just).flatMap(RenderUtils::render);
	}

	@Override
	@LimitRule(resource = "publish-config", qps = 500)
	@NeedAuth(argueName = "namespaceId", role = PropertiesEnum.Role.DEVELOPER)
	public Mono<ServerResponse> modifyConfig(ServerRequest request) {
		final String namespaceId = request.queryParam("namespaceId").orElse("default");
		return request.bodyToMono(PublishConfigRequest.class)
				.publishOn(Schedulers
						.fromExecutor(SchedulerUtils.getSingleton().WEB_HANDLER))
				.map(publishRequest -> operationService.modifyConfig(namespaceId,
						publishRequest))
				.map(Mono::just).flatMap(RenderUtils::render);
	}

	@Override
	@LimitRule(resource = "query-config", qps = 2000)
	@NeedAuth(argueName = "namespaceId")
	public Mono<ServerResponse> queryConfig(ServerRequest request) {
		final String namespaceId = request.queryParam("namespaceId").orElse("default");
		final String groupId = request.queryParam("groupId").orElse("DEFAULT_GROUP");
		final String dataId = request.queryParam("dataId").orElse("");
		final QueryConfigRequest queryRequest = QueryConfigRequest.sbuilder()
				.groupId(groupId).dataId(dataId).build();
		final String clientIp = request.headers().asHttpHeaders()
				.getFirst(StringConst.CLIENT_ID_NAME);
		queryRequest.setAttribute("clientIp", clientIp);
		Mono<ResponseData<?>> mono = Mono
				.just(operationService.queryConfig(namespaceId, queryRequest));
		return RenderUtils.render(mono).subscribeOn(
				Schedulers.fromExecutor(SchedulerUtils.getSingleton().WEB_HANDLER));
	}

	@Override
	@LimitRule(resource = "publish-config", qps = 500)
	@NeedAuth(argueName = "namespaceId", role = PropertiesEnum.Role.DEVELOPER)
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
		return RenderUtils.render(mono);
	}

	@Override
	@NeedAuth(argueName = "namespaceId")
	public Mono<ServerResponse> configList(ServerRequest request) {
		final String namespaceId = request.queryParam("namespaceId").orElse("default");
		final long page = Long.parseLong(request.queryParam("page").orElse("1"));
		final long pageSize = Long.parseLong(request.queryParam("pageSize").orElse("10"));
		final long lastId = Long.parseLong(request.queryParam("lastId").orElse("0"));
		Mono<ResponseData<ConfigListVO>> mono = Mono
				.just(operationService.configList(namespaceId, page, pageSize, lastId));
		return RenderUtils.render(mono);
	}

	@Override
	@NeedAuth(argueName = "namespaceId")
	public Mono<ServerResponse> configDetail(ServerRequest request) {
		final String namespaceId = request.queryParam("namespaceId").orElse("default");
		final String groupId = request.queryParam("groupId").orElse("DEFAULT_GROUP");
		final String dataId = request.queryParam("dataId").orElse("");
		Mono<ResponseData<ConfigDetailVO>> mono = Mono
				.just(operationService.configDetail(namespaceId, groupId, dataId));
		return RenderUtils.render(mono);
	}
}
