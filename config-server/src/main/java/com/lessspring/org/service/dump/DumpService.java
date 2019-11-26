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

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import com.lessspring.org.PathUtils;
import com.lessspring.org.repository.ConfigInfoMapper;
import com.lessspring.org.service.config.ConfigCacheItemManager;
import com.lessspring.org.service.dump.task.DumpTask4All;
import com.lessspring.org.service.dump.task.DumpTask4Beta;
import com.lessspring.org.service.dump.task.DumpTask4Period;
import com.lessspring.org.utils.PathConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component(value = "dumpService")
public class DumpService {

	private final DumpProcessor<DumpTask4All> task4AllDumpProcessor;
	private final DumpProcessor<DumpTask4Beta> task4BetaDumpProcessor;
	@Value("${com.lessspring.org.config-manager.environment}")
	private String developEnv;
	@Autowired
	private PathConstants pathConstants;
	@Resource
	private ConfigInfoMapper configInfoMapper;
	private DumpProcessor<DumpTask4Period> periodProcessor;

	public DumpService(ConfigCacheItemManager cacheItemManager) {
		this.task4AllDumpProcessor = new DumpAllProcessor(cacheItemManager,
				configInfoMapper);
		this.task4BetaDumpProcessor = new DumpAllBetaProcessor(cacheItemManager,
				configInfoMapper);
	}

	@PostConstruct
	public void init() {
		if (Objects.equals(developEnv, "develop")) {
			log.warn(
					"Development mode, the start automatically delete the last dump data");
			deleteWhenStart();
		}
		periodProcessor = new DumpPeriodProcessor(configInfoMapper, dumpAll(),
				dumpAllBeta());
		task4AllDumpProcessor.init();
		task4BetaDumpProcessor.init();
		periodProcessor.init();
		forceDump(true);
		DumpTask4Period period = new DumpTask4Period(true);
		period.setPeriod(Duration.ofMinutes(15));
		periodProcessor.process(period);
	}

	@PreDestroy
	public void destroy() {
		task4AllDumpProcessor.destroy();
		task4BetaDumpProcessor.destroy();
		periodProcessor.destroy();
	}

	public synchronized void forceDump(boolean async) {
		Long[] ids = configInfoMapper.findMinAndMaxId().toArray(new Long[0]);
		dumpAll().accept(ids, async);
		Long[] ids4Beta = configInfoMapper.findMinAndMaxId4Beta().toArray(new Long[0]);
		dumpAllBeta().accept(ids4Beta, async);
	}

	private void deleteWhenStart() {
		final String cachePath = PathUtils.finalPath("config-cache");
		try {
			FileUtils.deleteDirectory(new File(cachePath));
		}
		catch (IOException e) {
			log.error("Delete expired files error");
		}
	}

	private BiConsumer<Long[], Boolean> dumpAll() {
		return (ids, async) -> {
			if (Objects.nonNull(ids) && ids.length == 2) {
				int batchSize = 1_000;
				int counter = 0;
				List<Long> batchWork = new ArrayList<>(batchSize);
				for (long id = ids[0]; id < ids[1]; id++) {
					batchWork.add(id);
					counter++;
					if (counter > batchSize) {
						counter = 0;
						DumpTask4All task4All = new DumpTask4All(
								batchWork.toArray(new Long[0]), async);
						task4AllDumpProcessor.process(task4All);
						batchWork.clear();
					}
				}
			}
		};
	}

	private BiConsumer<Long[], Boolean> dumpAllBeta() {
		return (ids, async) -> {
			if (Objects.nonNull(ids) && ids.length == 2) {
				int batchSize = 1_000;
				int counter = 0;
				List<Long> batchWork = new ArrayList<>(batchSize);
				for (long id = ids[0]; id < ids[1]; id++) {
					batchWork.add(id);
					counter++;
					if (counter > batchSize) {
						counter = 0;
						DumpTask4Beta task4Beta = new DumpTask4Beta(
								batchWork.toArray(new Long[0]), async);
						task4BetaDumpProcessor.process(task4Beta);
						batchWork.clear();
					}
				}
			}
		};
	}

}
