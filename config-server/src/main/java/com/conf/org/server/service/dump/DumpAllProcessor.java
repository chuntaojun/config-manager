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
package com.conf.org.server.service.dump;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.conf.org.db.dto.ConfigInfoDTO;
import com.conf.org.executor.NameThreadFactory;
import com.conf.org.server.repository.ConfigInfoMapper;
import com.conf.org.server.service.cluster.DistroRouter;
import com.conf.org.server.service.config.ConfigCacheItemManager;
import com.conf.org.server.service.dump.task.DumpTask4All;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
public class DumpAllProcessor implements DumpProcessor<DumpTask4All> {

	private final ConfigCacheItemManager cacheItemManager;
	private final ConfigInfoMapper configInfoMapper;
	private final DistroRouter distroRouter = DistroRouter.getInstance();
	private final AtomicBoolean inited = new AtomicBoolean(false);
	private final AtomicBoolean destroyed = new AtomicBoolean(false);
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
					.forEach(configInfoDTO -> cacheItemManager
							.dumpConfig(configInfoDTO.getNamespaceId(), configInfoDTO));
		});
	}

	@Override
	public void init() {
		if (inited.compareAndSet(false, true)) {
			this.executor = Executors.newFixedThreadPool(4,
					new NameThreadFactory("com.lessspring.org.config.DumpAll-"));
		}
	}

	@Override
	public void destroy() {
		if (isInited() && destroyed.compareAndSet(false, true)) {
			this.executor.shutdown();
		}
	}

	@Override
	public boolean isInited() {
		return inited.get();
	}

	@Override
	public boolean isDestroyed() {
		return destroyed.get();
	}
}
