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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.lessspring.org.raft.NodeManager;
import com.lessspring.org.raft.TransactionIdManager;
import com.lessspring.org.raft.pojo.ServerNode;
import com.lessspring.org.raft.pojo.TransactionId;
import com.lessspring.org.server.utils.SnakflowerIdHelper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * Business id manager, according to the different TransactionId of business application,
 * so as to obtain a globally unique and monotone increasing id information
 * 
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component
public class ConfigTransactionIdManager implements TransactionIdManager {

	private final Map<String, TransactionId> manager = new HashMap<>(8);

	private final Object monitor = new Object();
	private final NodeManager nodeManager = NodeManager.getInstance();
	private int index = 0;
	private int workId = 0;

	@Override
	public void init() {
		Collection<ServerNode> allNodes = nodeManager.serverNodes();
		ServerNode self = nodeManager.getSelf();
		for (ServerNode tmp : allNodes) {
			if (Objects.equals(self, tmp)) {
				break;
			}
			index++;
		}
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
			transactionId.setSnakflowerIdHelper(new SnakflowerIdHelper(index, workId ++));
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
