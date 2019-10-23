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
package com.lessspring.org.raft;

import java.util.concurrent.atomic.AtomicBoolean;

import com.alipay.sofa.jraft.RaftGroupService;
import com.lessspring.org.LifeCycle;
import com.lessspring.org.raft.conf.RaftServerOptions;
import com.lessspring.org.raft.conf.RegionEngineOptions;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class RegionEngine implements LifeCycle {

	private final Region region;
	private final StoreEngine storeEngine;
	private final RegionEngineOptions regionOpts;

	private RaftGroupService raftGroupService;
	private ClusterServer serverNode;

	private final AtomicBoolean inited = new AtomicBoolean(false);
	private final AtomicBoolean destroyed = new AtomicBoolean(false);

	public RegionEngine(Region region, StoreEngine storeEngine,
			RegionEngineOptions regionOpts) {
		this.region = region;
		this.storeEngine = storeEngine;
		this.regionOpts = regionOpts;
	}

	@Override
	public void init() {
		if (inited.compareAndSet(false, true)) {
			RaftServerOptions configuration = RaftServerOptions.builder().build();
			serverNode = new ClusterServer(configuration);
		}
	}

	@Override
	public void destroy() {
		if (isInited() && destroyed.compareAndSet(false, true)) {

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
