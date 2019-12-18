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
package com.lessspring.org.server.service.dump;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import com.lessspring.org.executor.NameThreadFactory;
import com.lessspring.org.server.repository.ConfigInfoMapper;
import com.lessspring.org.server.service.dump.task.DumpTask4Period;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class DumpPeriodProcessor implements DumpProcessor<DumpTask4Period> {

	private final ConfigInfoMapper configInfoMapper;
	private final BiConsumer<Long[], Boolean> task4AllDumpProcessor;
	private final BiConsumer<Long[], Boolean> task4BetaDumpProcessor;
	private ScheduledThreadPoolExecutor executor;

	public DumpPeriodProcessor(ConfigInfoMapper configInfoMapper,
			BiConsumer<Long[], Boolean> task4AllDumpProcessor,
			BiConsumer<Long[], Boolean> task4BetaDumpProcessor) {
		this.configInfoMapper = configInfoMapper;
		this.task4AllDumpProcessor = task4AllDumpProcessor;
		this.task4BetaDumpProcessor = task4BetaDumpProcessor;

	}

	@Override
	public void process(DumpTask4Period dumpTask) {
		long seconds = dumpTask.getPeriod().getSeconds();
		// Task cycle
		executor.scheduleAtFixedRate(() -> {
			// Asynchronous dump mission operations
			Long[] ids = configInfoMapper.findMinAndMaxId().toArray(new Long[0]);
			task4AllDumpProcessor.accept(ids, dumpTask.isAsync());
			Long[] ids4Beta = configInfoMapper.findMinAndMaxId4Beta()
					.toArray(new Long[0]);
			task4BetaDumpProcessor.accept(ids4Beta, dumpTask.isAsync());
		}, seconds / 2, seconds, TimeUnit.SECONDS);
	}

	@Override
	public void init() {
		this.executor = new ScheduledThreadPoolExecutor(1,
				new NameThreadFactory("com.lessspring.org.server.service.dump.Executor"));
	}

	@Override
	public void destroy() {
		this.executor.shutdown();
	}

	@Override
	public boolean isInited() {
		return false;
	}

	@Override
	public boolean isDestroyed() {
		return false;
	}
}
