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

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class Transaction {

	private final long id;
	private final String key;
	private final String operation;
	private final byte[] data;

	public Transaction(long id, String key, byte[] data, String operation) {
		this.id = id;
		this.key = key;
		this.operation = operation;
		this.data = data;
	}

	public long getId() {
		return id;
	}

	public byte[] getData() {
		return data;
	}

	public String getOperation() {
		return operation;
	}

	public String getKey() {
		return key;
	}

}
