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
package com.lessspring.org.service.dump;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.lessspring.org.NameUtils;
import com.lessspring.org.db.dto.ConfigInfoDTO;
import com.lessspring.org.repository.ConfigInfoMapper;
import com.lessspring.org.service.cluster.DistroRouter;
import com.lessspring.org.service.config.ConfigCacheItemManager;
import com.lessspring.org.service.dump.task.DumpTask4All;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
public class DumpAllProcessor implements DumpProcessor<DumpTask4All> {

	private final ConfigCacheItemManager cacheItemManager;
	private final ConfigInfoMapper configInfoMapper;
	private final DistroRouter distroRouter = DistroRouter.getInstance();
	private ExecutorService executor;

	public DumpAllProcessor(ConfigCacheItemManager cacheItemManager,
			ConfigInfoMapper configInfoMapper) {
		this.cacheItemManager = cacheItemManager;
		this.configInfoMapper = configInfoMapper;

	}

	@Override
	public void process(DumpTask4All dumpTask) {
		executor.execute(() -> {
			List<ConfigInfoDTO> configInfoDTOS = configInfoMapper
					.batchFindConfigInfo(Arrays.asList(dumpTask.getIds()));
			configInfoDTOS.parallelStream()
					.filter(configInfoDTO -> distroRouter.isPrincipal(NameUtils.buildName(
							configInfoDTO.getNamespaceId(), configInfoDTO.getGroupId(),
							configInfoDTO.getDataId())))
					.forEach(configInfoDTO -> cacheItemManager
							.dumpConfig(configInfoDTO.getNamespaceId(), configInfoDTO));
		});
	}

	@Override
	public void init() {
		this.executor = Executors.newFixedThreadPool(4, new ThreadFactory() {

			AtomicInteger id = new AtomicInteger(0);

			@Override
			public Thread newThread(@NotNull Runnable r) {
				Thread thread = new Thread(r,
						"com.lessspring.org.config.DumpAll-" + id.getAndIncrement());
				thread.setDaemon(true);
				return thread;
			}
		});
	}

	@Override
	public void destroy() {
		this.executor.shutdown();
	}
}