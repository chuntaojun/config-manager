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

package com.conf.org.context;

import com.conf.org.executor.CForkJoinThread;
import com.conf.org.executor.CThread;

/**
 * A simple link tracing framework, custom implementation of threads and forkjointhreads,
 * low overhead passthrough of TraceContext
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019-11-28 16:28
 */
public final class TraceContextHolder {

	private final ThreadLocal<TraceContext> contextThreadLocal = ThreadLocal
			.<TraceContext> withInitial(TraceContext::new);

	private static final TraceContextHolder INSTANCE = new TraceContextHolder();

	public static TraceContextHolder getInstance() {
		return INSTANCE;
	}

	public TraceContext getInvokeTraceContext() {
		Thread thread = Thread.currentThread();
		if (thread instanceof CThread) {
			return ((CThread) thread).getTraceContext();
		}
		if (thread instanceof CForkJoinThread) {
			return ((CForkJoinThread) thread).getTraceContext();
		}
		return INSTANCE.contextThreadLocal.get();
	}

	public void setInvokeTraceContext(TraceContext context) {
		Thread thread = Thread.currentThread();
		if (thread instanceof CThread) {
			((CThread) thread).setTraceContext(context);
			return;
		}
		if (thread instanceof CForkJoinThread) {
			((CForkJoinThread) thread).setTraceContext(context);
			return;
		}
		INSTANCE.contextThreadLocal.set(context);
	}

	public void removeInvokeTraceContext() {
		Thread thread = Thread.currentThread();
		if (thread instanceof CThread) {
			((CThread) thread).cleanTraceContext();
			return;
		}
		if (thread instanceof CForkJoinThread) {
			((CForkJoinThread) thread).cleanTraceContext();
			return;
		}
		INSTANCE.contextThreadLocal.remove();
	}

}
