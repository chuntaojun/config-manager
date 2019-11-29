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
package com.lessspring.org.handler;

import reactor.core.publisher.Mono;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public interface SystemHandler {

	/**
	 * update logger level
	 * 
	 * @param request {@link ServerRequest}
	 * @return {@link Mono<ServerResponse>}
	 */
	Mono<ServerResponse> changeLogLevel(ServerRequest request);

	/**
	 * force dump config-file and config-beta-info
	 * 
	 * @param request {@link ServerRequest}
	 * @return {@link Mono<ServerResponse>}
	 */
	Mono<ServerResponse> forceDumpConfig(ServerRequest request);

	/**
	 * get publish log
	 *
	 * @param request {@link ServerRequest}
	 * @return {@link Mono<ServerResponse>}
	 */
	Mono<ServerResponse> publishLog(ServerRequest request);

	/**
	 * dump now jvm heap
	 *
	 * @param request {@link ServerRequest}
	 * @return {@link Mono<ServerResponse>}
	 */
	Mono<ServerResponse> jvmHeapDump(ServerRequest request);

	/**
	 * publish qps setting
	 *
	 * @param request {@link ServerRequest}
	 * @return {@link Mono<ServerResponse>}
	 */
	Mono<ServerResponse> publishQpsSetting(ServerRequest request);

	/**
	 * query qps setting
	 *
	 * @param request {@link ServerRequest}
	 * @return {@link Mono<ServerResponse>}
	 */
	Mono<ServerResponse> queryQpsSetting(ServerRequest request);

	/**
	 * Returns all transaction ID information
	 *
	 * @param request {@link ServerRequest}
	 * @return {@link Mono<ServerResponse>}
	 */
	Mono<ServerResponse> getAllTransactionIdInfo(ServerRequest request);

}
