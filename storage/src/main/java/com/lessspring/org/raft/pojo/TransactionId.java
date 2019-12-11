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
public class TransactionId implements Cloneable {

	private Long start = 0L;
	private Long end = 10000L;
	private final String bz;
	private Long id = 0L;

	public TransactionId(String bz) {
		this.bz = bz;
	}

	public Long getStart() {
		return start;
	}

	public void setStart(Long start) {
		this.start = start;
	}

	public Long getEnd() {
		return end;
	}

	public void setEnd(Long end) {
		this.end = end;
	}

	public String getBz() {
		return bz;
	}

	public Long getId() {
		return id;
	}

	public synchronized Long increaseAndObtain() {
		id += 1;
		return id;
	}

	public synchronized Long obtainAndIncrease() {
		Long tmp = id;
		id += 1;
		return tmp;
	}

	public synchronized void setId(Long id) {
		this.id = id;
	}

	public TransactionId saveOld() {
		TransactionId old = new TransactionId(bz);
		old.setStart(start);
		old.setEnd(end);
		old.setId(id);
		return old;
	}
}
