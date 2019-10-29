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
package com.lessspring.org.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.lessspring.org.BaseRejectedExecutionHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
public class SchedulerUtils {

	private static final int CORE_POOL_SIZE = Runtime.getRuntime()
			.availableProcessors() << 2;
	private static final int MAX_POOL_SIZE = 512;
	private static final long KEEP_ALIVE_TIME = 60;
	private static final TimeUnit UNIT = TimeUnit.SECONDS;

	private static ThreadFactory threadFactory = new ThreadFactory() {
		private final AtomicInteger nextId = new AtomicInteger(1);

		@Override
		public Thread newThread(@NotNull Runnable r) {
			String prefix = "com.lessspring.org.config-manager.webHandler";
			String name = prefix + nextId.getAndDecrement();
			return new Thread(r, name);
		}
	};

	private static RejectedExecutionHandler rejectedExecutionHandler = new BaseRejectedExecutionHandler(
			"com.lessspring.org.config-manager.webHandler", true,
			"com.lessspring.org.config-manager.webHandler") {
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			dumpJvmInfoIfNeeded();
			log.error("{} web custom thread task rejection {}", r.toString(),
					executor.toString());
		}
	};

	public static final ThreadPoolExecutor WEB_HANDLER = new ThreadPoolExecutor(
			CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, UNIT,
			new ArrayBlockingQueue<Runnable>(1024), threadFactory,
			rejectedExecutionHandler);

}
