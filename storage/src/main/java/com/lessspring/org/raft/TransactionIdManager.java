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

import com.lessspring.org.AsyncCallback;
import com.lessspring.org.raft.pojo.TransactionId;

import java.util.Map;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public interface TransactionIdManager {

	/**
	 * init
	 */
	void init();

	/**
	 * apply new ids
	 *
	 * @param transactionId {@link TransactionId
	 * @param retry retry times
	 * @param callback {@link AsyncCallback}
	 */
	void applyId(TransactionId transactionId, long retry, AsyncCallback callback);

	/**
	 * query {@link TransactionId} by bz
	 *
	 * @param bz business
	 * @return {@link TransactionId}
	 */
	TransactionId query(String bz);

	/**
	 * register {@link TransactionId}
	 * 
	 * @param transactionId {@link TransactionId}
	 */
	void register(TransactionId transactionId);

	/**
	 * deregister {@link TransactionId}
	 * 
	 * @param transactionId {@link TransactionId}
	 */
	void deregister(TransactionId transactionId);

	/**
	 * Returns all transaction ID information
	 *
	 * @return {@link Map<String, TransactionId>}
	 */
	Map<String, TransactionId> all();

	/**
	 * load from snapshot
	 *
	 * @param snapshot
	 */
	void snapshotLoad(Map<String, TransactionId> snapshot);

	/**
	 * this {@link TransactionIdManager} manager ID type
	 *
	 * @return label name
	 */
	String label();

}
