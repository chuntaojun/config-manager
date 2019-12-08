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
package com.lessspring.org.service.dump;

import com.lessspring.org.executor.NameThreadFactory;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.raft.exception.TransactionException;
import com.lessspring.org.raft.pojo.Datum;
import com.lessspring.org.raft.pojo.Transaction;
import com.lessspring.org.raft.utils.OperationEnum;
import com.lessspring.org.repository.ConfigInfoHistoryMapper;
import com.lessspring.org.service.cluster.ClusterManager;
import com.lessspring.org.service.cluster.FailCallback;
import com.lessspring.org.service.distributed.BaseTransactionCommitCallback;
import com.lessspring.org.service.distributed.TransactionConsumer;
import com.lessspring.org.utils.PropertiesEnum;
import com.lessspring.org.utils.RequireHelper;
import com.lessspring.org.utils.WaitFinish;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@WaitFinish
@Component
public class CleanProcessor {

	@Autowired
	private BaseTransactionCommitCallback commitCallback;

	@Autowired
	private ClusterManager clusterManager;

	@Resource
	private ConfigInfoHistoryMapper historyMapper;

	private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

	private FailCallback failCallback;

	public void init() {
		commitCallback.registerConsumer(PropertiesEnum.Bz.CONFIG,
				batchCleanHistoryConsumer(), OperationEnum.BATCH_DELETE.name());
		failCallback = throwable -> null;

		scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(4,
				new NameThreadFactory(
						"com.lessspring.org.config-manager.config.history.cleaner-"));
		scheduledThreadPoolExecutor.allowCoreThreadTimeOut(true);
		scheduledThreadPoolExecutor.setKeepAliveTime(60, TimeUnit.SECONDS);
	}

	private void autoRemoveHistoryConfig() {
		if (clusterManager.isLeader()) {
			Long[] ids = historyMapper.findMinAndMaxId().toArray(new Long[0]);
			RequireHelper.requireNotNull(ids, "Min and Max id not null");
			RequireHelper.requireEquals(ids.length, 2, "should be retuen two id num");
			Long minId = ids[0];
			Long maxId = ids[1];
		}
	}

	private TransactionConsumer<Transaction> batchCleanHistoryConsumer() {
		return new TransactionConsumer<Transaction>() {
			@Override
			public void accept(Transaction transaction) throws Throwable {

			}

			@Override
			public void onError(TransactionException te) {

			}
		};
	}

	private ResponseData<?> commit(Datum datum) {
		CompletableFuture<ResponseData<Boolean>> future = clusterManager.commit(datum,
				failCallback);
		try {
			return future.get(10_000L, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException | ExecutionException | TimeoutException e) {
			return ResponseData.fail(e);
		}
	}

}
