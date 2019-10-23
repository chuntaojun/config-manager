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

import com.lessspring.org.handler.SystemHandler;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.service.dump.DumpService;
import com.lessspring.org.utils.RenderUtils;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Service
public class SystemHandlerImpl implements SystemHandler {

	private final LoggingSystem loggingSystem;
	private final DumpService dumpService;

	public SystemHandlerImpl(LoggingSystem loggingSystem, DumpService dumpService) {
		this.loggingSystem = loggingSystem;
		this.dumpService = dumpService;
	}

	@NotNull
	@Override
	public Mono<ServerResponse> changeLogLevel(ServerRequest request) {
		final String logLevel = request.queryParam("logLevel").orElse("info");
		LogLevel newLevel;
		try {
			newLevel = LogLevel.valueOf(logLevel);
		}
		catch (Exception e) {
			newLevel = LogLevel.INFO;
		}
		LogLevel finalNewLevel = newLevel;
		loggingSystem.getLoggerConfigurations().forEach(
				conf -> loggingSystem.setLogLevel(conf.getName(), finalNewLevel));
		return RenderUtils.render(Mono.just(ResponseData.success()));
	}

	@NotNull
	@Override
	public Mono<ServerResponse> forceDumoConfig(ServerRequest request) {
		dumpService.forceDump(false);
		return RenderUtils.render(Mono.just(ResponseData.success()));
	}
}
