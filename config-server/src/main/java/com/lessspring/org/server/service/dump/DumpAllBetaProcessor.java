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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.lessspring.org.NameUtils;
import com.lessspring.org.db.dto.ConfigBetaInfoDTO;
import com.lessspring.org.executor.NameThreadFactory;
import com.lessspring.org.executor.ThreadPoolHelper;
import com.lessspring.org.server.repository.ConfigInfoMapper;
import com.lessspring.org.server.service.cluster.DistroRouter;
import com.lessspring.org.server.service.config.ConfigCacheItemManager;
import com.lessspring.org.server.service.dump.task.DumpTask4Beta;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class DumpAllBetaProcessor implements DumpProcessor<DumpTask4Beta> {

	private final ConfigCacheItemManager cacheItemManager;
	private final ConfigInfoMapper configInfoMapper;
	private final DistroRouter distroRouter = DistroRouter.getInstance();
	private final AtomicBoolean inited = new AtomicBoolean(false);
	private final AtomicBoolean destroyed = new AtomicBoolean(false);
	private ExecutorService executor;

	public DumpAllBetaProcessor(ConfigCacheItemManager cacheItemManager,
			ConfigInfoMapper configInfoMapper) {
		this.cacheItemManager = cacheItemManager;
		this.configInfoMapper = configInfoMapper;
	}

	@Override
	public void process(DumpTask4Beta dumpTask) {
		executor.execute(() -> {
			List<ConfigBetaInfoDTO> betaInfoDTOS = configInfoMapper
					.batchFindConfigInfo4Beta(Arrays.asList(dumpTask.getIds()));
			betaInfoDTOS.parallelStream()
					.filter(betaInfoDTO -> distroRouter
							.isPrincipal(NameUtils.buildName(betaInfoDTO.getNamespaceId(),
									betaInfoDTO.getGroupId(), betaInfoDTO.getDataId())))
					.forEach(configInfoDTO -> cacheItemManager.dumpConfigBeta(
							configInfoDTO.getNamespaceId(), configInfoDTO));
		});
	}

	@Override
	public void init() {
		if (inited.compareAndSet(false, true)) {
			this.executor = Executors.newFixedThreadPool(4,
					new NameThreadFactory("com.lessspring.org.config.DumpAllBeta-"));
		}
	}

	@Override
	public void destroy() {
		if (isInited() && destroyed.compareAndSet(false, true)) {
			ThreadPoolHelper.invokeShutdown(this.executor);
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
