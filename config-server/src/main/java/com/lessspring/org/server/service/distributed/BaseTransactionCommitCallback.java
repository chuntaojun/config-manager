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
package com.lessspring.org.server.service.distributed;

import java.util.HashMap;
import java.util.Map;

import com.lessspring.org.raft.TransactionCommitCallback;
import com.lessspring.org.raft.exception.TransactionException;
import com.lessspring.org.raft.pojo.Transaction;
import com.lessspring.org.server.exception.BaseException;
import com.lessspring.org.server.utils.PropertiesEnum;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component
public class BaseTransactionCommitCallback implements TransactionCommitCallback {

	private final HashMap<PropertiesEnum.Bz, Map<String, TransactionConsumer<Transaction>>> consumerMap = new HashMap<>(
			4);

	public void registerConsumer(PropertiesEnum.Bz bz,
			TransactionConsumer<Transaction> consumer, String operation) {
		synchronized (this) {
			consumerMap.computeIfAbsent(bz, b -> new HashMap<>(4));
			consumerMap.get(bz).put(operation, consumer);
		}
	}

	@Override
	public void onApply(Transaction transaction, String bzName)
			throws TransactionException {
		final PropertiesEnum.Bz bz = PropertiesEnum.Bz.valueOf(bzName);
		TransactionConsumer<Transaction> consumer = consumerMap.get(bz)
				.get(transaction.getOperation());
		try {
			consumer.accept(transaction);
		}
		catch (Throwable e) {
			log.info("error : {0}", e);
			TransactionException exception = new TransactionException(e);
			if (e instanceof BaseException) {
				exception.setErrorCode(((BaseException) e).code());
			}
			exception.setTransaction(transaction);
			consumer.onError(exception);
			consumer.rollBack();
			throw exception;
		}
	}

	@Override
	public String interest(String trsKey) {
		for (PropertiesEnum.InterestKey key : PropertiesEnum.InterestKey.values()) {
			if (trsKey.contains(key.getType())) {
				return key.name();
			}
		}
		throw new IllegalArgumentException(
				"Illegal transaction log, no business module to handle");
	}

}
