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

import com.lessspring.org.AsyncCallback;
import com.lessspring.org.raft.TransactionIdManager;

import java.util.Objects;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class TransactionId implements Cloneable {

	private Long start = 0L;
	private Long end = 10000L;
	private final String bz;
	private Long id = 0L;
	private volatile boolean inApply = false;
	private final TransactionIdManager manager;

	public TransactionId(String bz, TransactionIdManager manager) {
		this.bz = bz;
		this.manager = manager;
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
		while (inApply) {
			// await id apply
		}
		id += 1;
		if (Objects.equals(id, end)) {
			needToApply();
		}
		return id;
	}

	public synchronized Long obtainAndIncrease() {
		while (inApply) {
			// await id apply
		}
		Long tmp = id;
		id += 1;
		if (Objects.equals(id, end)) {
			needToApply();
		}
		return tmp;
	}

	public synchronized void setId(Long id) {
		this.id = id;
	}

	private void needToApply() {
		inApply = true;
		manager.applyId(this, 0, new AsyncCallback() {
			@Override
			public void onSuccess() {
				inApply = false;
			}
		});
	}

	public TransactionId saveOld() {
		TransactionId old = new TransactionId(bz, manager);
		old.setStart(start);
		old.setEnd(end);
		old.setId(id);
		return old;
	}
}
