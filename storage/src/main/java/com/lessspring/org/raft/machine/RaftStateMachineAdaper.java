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
package com.lessspring.org.raft.machine;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.core.StateMachineAdapter;
import com.alipay.sofa.jraft.entity.LeaderChangeContext;
import com.lessspring.org.ThreadPoolHelper;
import com.lessspring.org.raft.LeaderStatusListener;
import com.lessspring.org.raft.SnapshotOperate;
import com.lessspring.org.raft.TransactionCommitCallback;
import com.lessspring.org.raft.TransactionIdManager;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public abstract class RaftStateMachineAdaper extends StateMachineAdapter {

	private ExecutorService executorService = Executors.newFixedThreadPool(2);

	private final AtomicLong leaderTerm = new AtomicLong(-1L);

	private List<LeaderStatusListener> listeners = new CopyOnWriteArrayList<>();

	TransactionIdManager transactionIdManager;

	public void setTransactionIdManager(TransactionIdManager transactionIdManager) {
		this.transactionIdManager = transactionIdManager;
	}

	/**
	 * register listener of {@link LeaderStatusListener}
	 * 
	 * @param listener {@link LeaderStatusListener}
	 */
	public void registerLeaderStatusListener(LeaderStatusListener listener) {
		listeners.add(listener);
	}

	/**
	 * Register the transaction callback interface
	 *
	 * @param commitCallback {@link TransactionCommitCallback}
	 */
	public abstract void registerTransactionCommitCallback(
			TransactionCommitCallback commitCallback);

	/**
	 * Register the snapshot operator
	 *
	 * @param snapshotOperate {@link SnapshotOperate}
	 */
	public abstract void registerSnapshotManager(SnapshotOperate snapshotOperate);

	@Override
	public void onLeaderStart(final long term) {
		super.onLeaderStart(term);
		this.leaderTerm.set(term);
	}

	@Override
	public void onLeaderStop(final Status status) {
		super.onLeaderStop(status);
		this.leaderTerm.set(-1L);
	}

	@Override
	public void onStopFollowing(LeaderChangeContext ctx) {
		super.onStopFollowing(ctx);
		String key = ctx.getLeaderId().getEndpoint().toString();
		executorService.execute(
				() -> listeners.forEach(leaderStatusListener -> leaderStatusListener
						.onLeaderStart(key, ctx.getTerm())));
	}

	@Override
	public void onStartFollowing(LeaderChangeContext ctx) {
		super.onStartFollowing(ctx);
		String key = ctx.getLeaderId().getEndpoint().toString();
		executorService.execute(
				() -> listeners.forEach(leaderStatusListener -> leaderStatusListener
						.onLeaderStop(key, ctx.getTerm())));
	}

	@Override
	public void onShutdown() {
		super.onShutdown();
		ThreadPoolHelper.invokeShutdown(executorService);
	}

	public boolean isLeader() {
		return leaderTerm.get() > 0;
	}

}
