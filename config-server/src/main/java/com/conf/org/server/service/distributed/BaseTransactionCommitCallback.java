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
package com.conf.org.server.service.distributed;

import java.util.HashMap;
import java.util.Map;

import com.conf.org.raft.TransactionCommitCallback;
import com.conf.org.raft.exception.TransactionException;
import com.conf.org.raft.pojo.Transaction;
import com.conf.org.server.exception.BaseException;
import com.conf.org.server.utils.PropertiesEnum;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component
public class BaseTransactionCommitCallback implements TransactionCommitCallback {

	private final HashMap<PropertiesEnum.Bz, Map<String, TransactionConsumer<Transaction>>> consumerMap = new HashMap<>(
			4);

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
		for (PropertiesEnum.Bz key : PropertiesEnum.Bz.values()) {
			if (trsKey.contains(key.name())) {
				return key.name();
			}
		}
		throw new IllegalArgumentException(
				"Illegal transaction log, no business module to handle");
	}

}