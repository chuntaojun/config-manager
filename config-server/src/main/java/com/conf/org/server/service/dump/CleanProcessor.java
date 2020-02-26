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

import java.util.Collections;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import com.conf.org.executor.NameThreadFactory;
import com.conf.org.server.repository.ConfigInfoHistoryMapper;
import com.conf.org.server.service.cluster.ClusterManager;
import com.conf.org.server.service.cluster.FailCallback;
import com.conf.org.server.utils.RequireHelper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Component
@Slf4j
public class CleanProcessor {

	@Autowired
	private ClusterManager clusterManager;

	@Resource
	private ConfigInfoHistoryMapper historyMapper;

	private ScheduledThreadPoolExecutor cleanMaster;

	private ScheduledThreadPoolExecutor cleanWorker;

	private FailCallback failCallback;

	public void init() {
		failCallback = throwable -> null;

		cleanMaster = new ScheduledThreadPoolExecutor(1, new NameThreadFactory(
				"com.lessspring.org.config-manager.config.history.cleaner"));

		// open auto clean config-history work
		cleanMaster.scheduleAtFixedRate(this::autoRemoveHistoryConfig, 15L, 30L,
				TimeUnit.MINUTES);

		cleanWorker = new ScheduledThreadPoolExecutor(4, new NameThreadFactory(
				"com.lessspring.org.config-manager.config.history.cleanWorker-"));

		cleanWorker.allowCoreThreadTimeOut(true);
		cleanWorker.setKeepAliveTime(60, TimeUnit.SECONDS);
	}

	private void autoRemoveHistoryConfig() {
		// only server-cluster leader can open clean config-history work
		if (clusterManager.isLeader()) {
			Long[] ids = historyMapper.findMinAndMaxId().toArray(new Long[0]);
			RequireHelper.requireNotNull(ids, "Min and Max id not null");
			RequireHelper.requireEquals(ids.length, 2, "should be return two id num");
			Long minId = ids[0];
			Long maxId = ids[1];
			for (long index = minId; index <= maxId; index++) {
				final long loc = index;
				historyMapper.batchDelete(Collections.singletonList(loc));
			}
		}
	}

}
