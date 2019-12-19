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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.lessspring.org.CasReadWriteLock;
import com.lessspring.org.raft.NodeManager;
import com.lessspring.org.raft.TransactionIdManager;
import com.lessspring.org.raft.pojo.ServerNode;
import com.lessspring.org.raft.pojo.TransactionId;
import com.lessspring.org.SnakflowerIdHelper;
import com.lessspring.org.server.utils.BzConstants;
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

	private final NodeManager nodeManager = NodeManager.getInstance();
	private int index = 0;
	private int workId = 0;

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
	private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

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
		readLock.lock();
		try {
			for (String item : BzConstants.bzs()) {
				if (bz.contains(item)) {
					return manager.get(item);
				}
			}
			return null;
		}
		finally {
			readLock.unlock();
		}
	}

	@Override
	public void register(TransactionId transactionId) {
		String bz = transactionId.getBz();
		writeLock.lock();
		try {
			transactionId.setSnakflowerIdHelper(new SnakflowerIdHelper(index, workId++));
			manager.putIfAbsent(bz, transactionId);
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public void deregister(TransactionId transactionId) {
		String bz = transactionId.getBz();
		writeLock.lock();
		try {
			manager.remove(bz);
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public Map<String, TransactionId> all() {
		return new HashMap<>(manager);
	}

	@Override
	public String label() {
		return "transaction-id-manager/config";
	}
}
