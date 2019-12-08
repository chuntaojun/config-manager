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
package com.lessspring.org.service.config;

import com.lessspring.org.raft.TransactionIdManager;
import com.lessspring.org.raft.pojo.TransactionId;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Business id manager, according to the different TransactionId of business application,
 * so as to obtain a globally unique and monotone increasing id information
 * 
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Component
public class ConfigTransactionIdManager implements TransactionIdManager {

	private final Map<String, TransactionId> manager = new HashMap<>(8);

	private final Object monitor = new Object();

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
