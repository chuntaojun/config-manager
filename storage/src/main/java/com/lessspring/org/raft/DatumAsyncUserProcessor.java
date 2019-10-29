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
package com.lessspring.org.raft;

import java.util.concurrent.CompletableFuture;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.raft.pojo.Datum;
import com.lessspring.org.raft.pojo.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
public class DatumAsyncUserProcessor extends BaseAsyncUserProcessor<Datum> {

	private static final String INTEREST_NAME = Datum.class.getName();

	@Override
	public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, Datum request) {
		try {
			CompletableFuture<ResponseData<Boolean>> future = clusterServer
					.apply(request);
			asyncCtx.sendResponse(
					Response.builder().success(future.get().getData()).build());
		}
		catch (Exception e) {
			log.error("Fail to apply follower request : {}", request);
			asyncCtx.sendResponse(
					Response.builder().success(false).errMsg(e.getMessage()).build());
		}
	}

	@Override
	public String interest() {
		return INTEREST_NAME;
	}

	@Override
	public void initCluster(ClusterServer clusterServer) {
		this.clusterServer = clusterServer;
	}
}
