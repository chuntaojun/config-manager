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
package com.conf.org.server.handler.impl;

import com.conf.org.constant.StringConst;
import com.conf.org.model.vo.ConfigQueryPage;
import com.conf.org.model.vo.DeleteConfigRequest;
import com.conf.org.model.vo.PublishConfigRequest;
import com.conf.org.model.vo.QueryConfigRequest;
import com.conf.org.model.vo.ResponseData;
import com.conf.org.server.service.config.OperationService;
import com.conf.org.server.handler.ConfigHandler;
import com.conf.org.server.pojo.vo.ConfigDetailVO;
import com.conf.org.server.pojo.vo.ConfigListVO;
import com.conf.org.server.utils.RenderUtils;
import com.conf.org.server.utils.SchedulerUtils;
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
@Service(value = "configHandler")
public class ConfigHandlerImpl implements ConfigHandler {

	private final OperationService operationService;

	public ConfigHandlerImpl(OperationService operationService) {
		this.operationService = operationService;
	}

	@Override
	public Mono<ServerResponse> publishConfig(ServerRequest request) {
		final String namespaceId = request.queryParam("namespaceId").orElse("default");
		return request.bodyToMono(PublishConfigRequest.class)
				.map(publishRequest -> operationService.publishConfig(namespaceId,
						publishRequest))
				.map(Mono::just).flatMap(RenderUtils::render);
	}

	@Override
	public Mono<ServerResponse> modifyConfig(ServerRequest request) {
		final String namespaceId = request.queryParam("namespaceId").orElse("default");
		return request.bodyToMono(PublishConfigRequest.class)
				.map(publishRequest -> operationService.modifyConfig(namespaceId,
						publishRequest))
				.map(Mono::just).flatMap(RenderUtils::render);
	}

	@Override
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
	public Mono<ServerResponse> configList(ServerRequest request) {
		final String namespaceId = request.queryParam("namespaceId").orElse("default");
		final long offset = Long.parseLong(request.queryParam("offset").orElse("1"));
		final long limit = Long.parseLong(request.queryParam("limit").orElse("10"));
		final ConfigQueryPage queryPage = ConfigQueryPage.builder()
				.namespaceId(namespaceId)
				.groupId(request.queryParam("groupId").orElse(""))
				.dataId(request.queryParam("dataId").orElse(""))
				.limit(limit)
				.offset(offset)
				.build();
		Mono<ResponseData<ConfigListVO>> mono = Mono
				.just(operationService.configList(queryPage));
		return RenderUtils.render(mono);
	}

	@Override
	public Mono<ServerResponse> configDetail(ServerRequest request) {
		final String namespaceId = request.queryParam("namespaceId").orElse("default");
		final String groupId = request.queryParam("groupId").orElse("DEFAULT_GROUP");
		final String dataId = request.queryParam("dataId").orElse("");
		Mono<ResponseData<ConfigDetailVO>> mono = Mono
				.just(operationService.configDetail(namespaceId, groupId, dataId));
		return RenderUtils.render(mono);
	}
}
