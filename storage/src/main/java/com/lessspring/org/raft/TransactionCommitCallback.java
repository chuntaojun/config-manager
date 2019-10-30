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

import com.lessspring.org.raft.exception.TransactionException;
import com.lessspring.org.raft.pojo.Transaction;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public interface TransactionCommitCallback {

	/**
	 * When raft transaction is completed, triggered the callback achieve business
	 *
	 * @param transaction {@link Transaction}
	 * @throws TransactionException
	 */
	void onApply(Transaction transaction) throws TransactionException;

	/**
	 * To determine transaction key, if interested
	 *
	 * @param trsKey Transaction key
	 * @return is interest
	 */
	boolean interest(String trsKey);

}
