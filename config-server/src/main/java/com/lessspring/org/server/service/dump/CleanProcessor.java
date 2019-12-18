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
package com.lessspring.org.server.service.dump;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import com.lessspring.org.executor.NameThreadFactory;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.raft.exception.TransactionException;
import com.lessspring.org.raft.pojo.Datum;
import com.lessspring.org.raft.pojo.Transaction;
import com.lessspring.org.raft.utils.OperationEnum;
import com.lessspring.org.server.pojo.request.DeleteConfigHistory;
import com.lessspring.org.server.repository.ConfigInfoHistoryMapper;
import com.lessspring.org.server.service.cluster.ClusterManager;
import com.lessspring.org.server.service.cluster.FailCallback;
import com.lessspring.org.server.service.distributed.BaseTransactionCommitCallback;
import com.lessspring.org.server.service.distributed.TransactionConsumer;
import com.lessspring.org.server.utils.GsonUtils;
import com.lessspring.org.server.utils.PropertiesEnum;
import com.lessspring.org.server.utils.RequireHelper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Component
@Slf4j
public class CleanProcessor {

	@Autowired
	private BaseTransactionCommitCallback commitCallback;

	@Autowired
	private ClusterManager clusterManager;

	@Resource
	private ConfigInfoHistoryMapper historyMapper;

	private ScheduledThreadPoolExecutor cleanMaster;

	private ScheduledThreadPoolExecutor cleanWorker;

	private FailCallback failCallback;

	public void init() {
		commitCallback.registerConsumer(PropertiesEnum.Bz.CONFIG,
				batchCleanHistoryConsumer(), OperationEnum.BATCH_DELETE.name());
		failCallback = throwable -> null;

		cleanMaster = new ScheduledThreadPoolExecutor(1, new NameThreadFactory(
				"com.lessspring.org.config-manager.config.history.cleaner"));

		// open auto clean config-history work
		cleanMaster.scheduleAtFixedRate(this::autoRemoveHistoryConfig, 15L, 30L,
				TimeUnit.MINUTES);

		cleanWorker = new ScheduledThreadPoolExecutor(4, new NameThreadFactory(
				"com.lessspring.org.config-manager.config.history.cleanWorker-"));

		cleanWorker.allowCoreThreadTimeOut(true);
		cleanWorker.setKeepAliveTime(60, TimeUnit.SECONDS);
	}

	private void autoRemoveHistoryConfig() {
		// only server-cluster leader can open clean config-history work
		if (clusterManager.isLeader()) {
			Long[] ids = historyMapper.findMinAndMaxId().toArray(new Long[0]);
			RequireHelper.requireNotNull(ids, "Min and Max id not null");
			RequireHelper.requireEquals(ids.length, 2, "should be return two id num");
			Long minId = ids[0];
			Long maxId = ids[1];
			for (long index = minId; index <= maxId; index++) {
				final long loc = index;
				cleanWorker.execute(() -> {
					final Datum datum = Datum.builder()
							.key("delete_config_history_id_" + loc)
							.value(GsonUtils.toJsonBytes(
									DeleteConfigHistory.dBuild().id(loc).build()))
							.operation(OperationEnum.BATCH_DELETE.name())
							.className(DeleteConfigHistory.CLASS_NAME).build();
					ResponseData<Boolean> result = commit(datum);
					log.info("[CleanProcessor] auto clean config-history result : {}",
							result);
				});
			}
		}
	}

	private TransactionConsumer<Transaction> batchCleanHistoryConsumer() {
		return new TransactionConsumer<Transaction>() {
			@Override
			public void accept(Transaction transaction) throws Throwable {
				DeleteConfigHistory request = GsonUtils.toObj(transaction.getData(),
						DeleteConfigHistory.class);
				historyMapper.batchDelete(Collections.singletonList(request.getId()));
			}

			@Override
			public void onError(TransactionException te) {
				log.error("[CleanConfigHistory Worker] error : {}", te);
			}
		};
	}

	private ResponseData<Boolean> commit(Datum datum) {
		datum.setBz(PropertiesEnum.Bz.CONFIG.name());
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
