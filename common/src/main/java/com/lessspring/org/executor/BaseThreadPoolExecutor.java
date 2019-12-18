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
package com.lessspring.org.executor;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class BaseThreadPoolExecutor extends ThreadPoolExecutor {

	private static final Logger logger = Logger
			.getLogger("com.lessspring.org.executor.BaseThreadPoolExecutor");

	private static final ThreadLocal<Long> workCostTimeLocal = ThreadLocal
			.withInitial(System::currentTimeMillis);

	private boolean openWorkCostDisplay = false;

	public void setOpenWorkCostDisplay(boolean openWorkCostDisplay) {
		this.openWorkCostDisplay = openWorkCostDisplay;
	}

	public BaseThreadPoolExecutor(int corePoolSize) {
		this(corePoolSize, Runtime.getRuntime().availableProcessors(), 60,
				TimeUnit.SECONDS, new LinkedBlockingQueue<>());
	}

	public BaseThreadPoolExecutor(int corePoolSize, NameThreadFactory threadFactory) {
		this(corePoolSize, Runtime.getRuntime().availableProcessors(), 60,
				TimeUnit.SECONDS, new LinkedBlockingQueue<>(), threadFactory);
	}

	public BaseThreadPoolExecutor(int corePoolSize, long keepAliveTime, TimeUnit unit) {
		this(corePoolSize, Runtime.getRuntime().availableProcessors(), keepAliveTime,
				unit, new LinkedBlockingQueue<>());
	}

	public BaseThreadPoolExecutor(int corePoolSize, long keepAliveTime, TimeUnit unit,
			NameThreadFactory factory) {
		this(corePoolSize, Runtime.getRuntime().availableProcessors(), keepAliveTime,
				unit, new LinkedBlockingQueue<>(), factory);
	}

	public BaseThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit) {
		this(corePoolSize, maximumPoolSize, keepAliveTime, unit,
				new LinkedBlockingQueue<>());
	}

	public BaseThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}

	public BaseThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
			NameThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory);
	}

	public BaseThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
			RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
	}

	public BaseThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
			NameThreadFactory threadFactory, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory, handler);
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		if (openWorkCostDisplay) {
			logger.info(MessageFormat.format("{0} start work",
					Thread.currentThread().getName()));
			workCostTimeLocal.get();
		}
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		if (openWorkCostDisplay) {
			logger.info(MessageFormat.format("{0} end work, spend time : {1}",
					Thread.currentThread().getName(),
					System.currentTimeMillis() - workCostTimeLocal.get()));
			workCostTimeLocal.remove();
			CThread cThread = (CThread) Thread.currentThread();
			cThread.cleanTraceContext();
		}
	}

	@Override
	public void execute(Runnable command) {
		super.execute(new WrapperRunnable(command));
	}

	@Override
	public boolean remove(Runnable task) {
		return super.remove(new WrapperRunnable(task));
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
		return super.newTaskFor(new WrapperRunnable(runnable), value);
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
		return super.newTaskFor(new WrapperCallable<>(callable));
	}

	@Override
	public Future<?> submit(Runnable task) {
		return super.submit(new WrapperRunnable(task));
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return super.submit(new WrapperRunnable(task), result);
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return super.submit(new WrapperCallable<>(task));
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
			throws InterruptedException, ExecutionException {
		return super.invokeAny(
				tasks.stream().map(WrapperCallable::new).collect(Collectors.toList()));
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout,
			TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return super.invokeAny(
				tasks.stream().map(WrapperCallable::new).collect(Collectors.toList()),
				timeout, unit);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
			throws InterruptedException {
		return super.invokeAll(
				tasks.stream().map(WrapperCallable::new).collect(Collectors.toList()));
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
			long timeout, TimeUnit unit) throws InterruptedException {
		return super.invokeAll(
				tasks.stream().map(WrapperCallable::new).collect(Collectors.toList()),
				timeout, unit);
	}

}
