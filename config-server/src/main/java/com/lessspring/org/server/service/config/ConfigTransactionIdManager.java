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
package com.lessspring.org.server.service.config;

import com.lessspring.org.AsyncCallback;
import com.lessspring.org.IDUtils;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.raft.NodeManager;
import com.lessspring.org.raft.TransactionIdManager;
import com.lessspring.org.raft.exception.TransactionException;
import com.lessspring.org.raft.pojo.Datum;
import com.lessspring.org.raft.pojo.Transaction;
import com.lessspring.org.raft.pojo.TransactionId;
import com.lessspring.org.server.pojo.request.IDRequest;
import com.lessspring.org.server.service.cluster.ClusterManager;
import com.lessspring.org.server.service.distributed.BaseTransactionCommitCallback;
import com.lessspring.org.server.service.distributed.TransactionConsumer;
import com.lessspring.org.server.utils.GsonUtils;
import com.lessspring.org.server.utils.PropertiesEnum;
import com.lessspring.org.server.utils.TransactionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Business id manager, according to the different TransactionId of business application,
 * so as to obtain a globally unique and monotone increasing id information
 * 
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component
public class ConfigTransactionIdManager implements TransactionIdManager {

	private final Map<String, TransactionId> manager = new HashMap<>(8);

	private final Object monitor = new Object();
	private final NodeManager nodeManager = NodeManager.getInstance();
	@Autowired
	@Lazy
	private ClusterManager clusterManager;
	@Autowired
	@Lazy
	private BaseTransactionCommitCallback commitCallback;

	@Override
	public void init() {

		commitCallback.registerConsumer(PropertiesEnum.Bz.TRANSACTION_ID_MANAGER,
				new TransactionConsumer<Transaction>() {

					Map<Byte, TransactionId> oldMap = new HashMap<>(8);

					@Override
					public void accept(Transaction transaction) throws Throwable {
						IDRequest request = GsonUtils.toObj(transaction.getData(),
								IDRequest.class);
						final String self = request.getLocalName();
						final TransactionId transactionId;
						if (!manager.containsKey(request.getLabel())) {
							transactionId = new TransactionId(request.getLabel(), ConfigTransactionIdManager.this);
							transactionId.setStart(request.getStart());
							transactionId.setEnd(request.getEnd());
							if (nodeManager.isSelf(self)) {
								transactionId.setId(request.getStart());
							}
							manager.put(request.getLabel(), transactionId);
							oldMap.put((byte) -1, transactionId.saveOld());
						}
						else {
							// 如果当前申请ID序列的可以进入
							transactionId = manager.get(request.getLabel());
							oldMap.put((byte) 1, transactionId.saveOld());
							long originEnd = transactionId.getEnd();
							if (originEnd < request.getStart()) {
								transactionId.setStart(request.getStart());
								transactionId.setEnd(request.getEnd());
								if (nodeManager.isSelf(self)) {
									transactionId.setId(request.getStart());
								}
								manager.put(request.getLabel(), transactionId);
							}
							throw new TransactionException("[{" + request.getLabel()
									+ "}] ID application conflict");
						}
					}

					@Override
					public void rollBack() {
						for (Map.Entry<Byte, TransactionId> item : oldMap.entrySet()) {
							if (item.getKey() == (byte) -1) {
								manager.remove(item.getValue().getBz());
							}
							else {
								manager.put(item.getValue().getBz(), item.getValue());
							}
						}
					}
				}, "apply");

		manager.forEach((s, transactionId) -> applyId(transactionId, 0, AsyncCallback.DEFAULT_ASYNC_CALLBACK));

	}

	@Override
	public void applyId(TransactionId transactionId, long retry, AsyncCallback callback) {
		int maxRetry = 3;
		long start = transactionId.getStart() + retry * 10000L + (retry == 0 ? 0 : 1);
		long end = start + 10000L;
		final IDRequest request = IDRequest.builder()
				.label(transactionId.getBz()).start(start).end(end).build();
		log.info("[TransactionIdManager] init : \n{}", GsonUtils.toJson(request));
		String key = TransactionUtils.buildTransactionKey(
				PropertiesEnum.InterestKey.TRANSACTION_ID_MANAGER,
				IDUtils.generateBase64(GsonUtils.toJson(request)));

		CompletableFuture<ResponseData<Boolean>> future = clusterManager
				.commit(Datum.builder().bz(PropertiesEnum.Bz.ID.name())
						.value(GsonUtils.toJsonBytes(request))
						.className(IDRequest.class.getCanonicalName()).operation("apply")
						.key(key).build(), throwable -> null);

		future.thenAccept(booleanResponseData -> {
			if (!booleanResponseData.getData()) {
				long tmpRetry = retry + 1;
				if (tmpRetry == maxRetry) {
					callback.onFail();
					return;
				}
				applyId(transactionId,retry + 1, callback);
			} else {
				callback.onSuccess();
			}
		});
	}

	@Override
	public TransactionId query(String bz) {
		synchronized (monitor) {
			return manager.get(bz);
		}
	}

	@Override
	public void register(TransactionId transactionId) {
		String bz = transactionId.getBz();
		synchronized (monitor) {
			manager.putIfAbsent(bz, transactionId);
		}
	}

	@Override
	public void deregister(TransactionId transactionId) {
		String bz = transactionId.getBz();
		synchronized (monitor) {
			manager.remove(bz);
		}
	}

	@Override
	public Map<String, TransactionId> all() {
		return new HashMap<>(manager);
	}

	@Override
	public void snapshotLoad(Map<String, TransactionId> snapshot) {
		synchronized (monitor) {
			manager.clear();
			manager.putAll(snapshot);
		}
	}

	@Override
	public String label() {
		return "transaction-id-manager/config";
	}
}
