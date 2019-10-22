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
package com.lessspring.org.raft.conf;

import com.lessspring.org.PathUtils;
import com.lessspring.org.raft.Region;
import com.lessspring.org.raft.StoreEngine;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Data
@Builder
@NoArgsConstructor
public class RaftServerOptions {

	private String cacheDir = PathUtils.finalPath("config-manager/server/config_manager_raft");
	private int electionTimeoutMs = (int) Duration.ofSeconds(5).toMillis();
	private int snapshotIntervalSecs = (int) Duration.ofSeconds(600).getSeconds();
	private Region region;
	private StoreEngine storeEngine;
	private String logUri;
	private String raftMetaUri;
	private String snapshotUri;
	private boolean startRpcServer = true;

}
