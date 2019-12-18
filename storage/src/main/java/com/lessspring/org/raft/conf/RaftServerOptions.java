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

import java.time.Duration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RaftServerOptions {

	private String cacheDir;
	private int electionTimeoutMs = (int) Duration.ofSeconds(5).toMillis();
	private int snapshotIntervalSecs = (int) Duration.ofSeconds(600).getSeconds();
	private String logUri = "raft-log";
	private String raftMetaUri = "raft-meta";
	private String snapshotUri = "raft-snapshot";
	private boolean startRpcServer = true;

	public static RaftServerOptionsBuilder builder() {
		return new RaftServerOptionsBuilder();
	}

	public static final class RaftServerOptionsBuilder {
		private String cacheDir;
		private int electionTimeoutMs = (int) Duration.ofSeconds(5).toMillis();
		private int snapshotIntervalSecs = (int) Duration.ofSeconds(600).getSeconds();
		private String logUri = "raft-log";
		private String raftMetaUri = "raft-meta";
		private String snapshotUri = "raft-snapshot";
		private boolean startRpcServer = true;

		private RaftServerOptionsBuilder() {
		}

		public RaftServerOptionsBuilder cacheDir(String cacheDir) {
			this.cacheDir = cacheDir;
			return this;
		}

		public RaftServerOptionsBuilder electionTimeoutMs(Integer electionTimeoutMs) {
			this.electionTimeoutMs = electionTimeoutMs;
			return this;
		}

		public RaftServerOptionsBuilder snapshotIntervalSecs(
				Integer snapshotIntervalSecs) {
			this.snapshotIntervalSecs = snapshotIntervalSecs;
			return this;
		}

		public RaftServerOptionsBuilder logUri(String logUri) {
			this.logUri = logUri;
			return this;
		}

		public RaftServerOptionsBuilder raftMetaUri(String raftMetaUri) {
			this.raftMetaUri = raftMetaUri;
			return this;
		}

		public RaftServerOptionsBuilder snapshotUri(String snapshotUri) {
			this.snapshotUri = snapshotUri;
			return this;
		}

		public RaftServerOptionsBuilder startRpcServer(boolean startRpcServer) {
			this.startRpcServer = startRpcServer;
			return this;
		}

		public RaftServerOptions build() {
			RaftServerOptions raftServerOptions = new RaftServerOptions();
			raftServerOptions.setCacheDir(cacheDir);
			raftServerOptions.setElectionTimeoutMs(electionTimeoutMs);
			raftServerOptions.setSnapshotIntervalSecs(snapshotIntervalSecs);
			raftServerOptions.setLogUri(logUri);
			raftServerOptions.setRaftMetaUri(raftMetaUri);
			raftServerOptions.setSnapshotUri(snapshotUri);
			raftServerOptions.setStartRpcServer(startRpcServer);
			return raftServerOptions;
		}
	}
}
