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
package com.lessspring.org.raft.pojo;

import com.lessspring.org.SnakflowerIdHelper;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class TransactionId implements Cloneable {

	private final String bz;
	private Long nowId;
	private SnakflowerIdHelper snakflowerIdHelper;

	public TransactionId(String bz) {
		this.bz = bz;
	}

	public void setSnakflowerIdHelper(SnakflowerIdHelper snakflowerIdHelper) {
		this.snakflowerIdHelper = snakflowerIdHelper;
		this.nowId = snakflowerIdHelper.nextId();
	}

	public String getBz() {
		return bz;
	}

	public Long getNowId() {
		return nowId;
	}

	public synchronized Long increaseAndObtain() {
		long tmpId = nowId;
		nowId = snakflowerIdHelper.nextId();
		return tmpId;
	}

}
