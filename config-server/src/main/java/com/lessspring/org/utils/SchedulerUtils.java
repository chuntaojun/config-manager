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

import com.lessspring.org.executor.BaseRejectedExecutionHandler;
import com.lessspring.org.executor.BaseThreadPoolExecutor;
import com.lessspring.org.executor.NameThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
public class SchedulerUtils {

	private static final SchedulerUtils INSTANCE = new SchedulerUtils();
	private static final int CORE_POOL_SIZE = 512;
	private static final int MAX_POOL_SIZE = 1024;
	private static final long KEEP_ALIVE_TIME = 60;
	private final SystemEnv systemEnv = SystemEnv.getSingleton();
	private NameThreadFactory threadFactory = new NameThreadFactory(
			"com.lessspring.org.config-manager.webHandler-");
	private RejectedExecutionHandler rejectedExecutionHandler = new BaseRejectedExecutionHandler(
			"com.lessspring.org.config-manager.webHandler", true,
			"com.lessspring.org.config-manager.webHandler") {
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			dumpJvmInfoIfNeeded();
			log.error("{} web custom thread task rejection {}", r.toString(),
					executor.toString());
		}
	};
	public final ThreadPoolExecutor WEB_HANDLER = newThreadPoolExecutor();

	public static SchedulerUtils getSingleton() {
		return INSTANCE;
	}

	private ThreadPoolExecutor newThreadPoolExecutor() {
		BaseThreadPoolExecutor executor = new BaseThreadPoolExecutor(CORE_POOL_SIZE,
				MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(8), threadFactory,
				rejectedExecutionHandler);
		executor.allowCoreThreadTimeOut(true);
		executor.setOpenWorkCostDisplay(systemEnv.isOpenWorkCostDisplay());
		return executor;
	}

}
