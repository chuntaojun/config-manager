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

import java.time.Duration;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class RaftConfiguration {

    private String cacheDir = "";
    private int electionTimeoutMs = (int) Duration.ofSeconds(5).toMillis();
    private int snapshotIntervalSecs = (int) Duration.ofSeconds(600).getSeconds();

    public String getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(String cacheDir) {
        this.cacheDir = cacheDir;
    }

    public int getElectionTimeoutMs() {
        return electionTimeoutMs;
    }

    public void setElectionTimeoutMs(int electionTimeoutMs) {
        this.electionTimeoutMs = electionTimeoutMs;
    }

    public int getSnapshotIntervalSecs() {
        return snapshotIntervalSecs;
    }

    public void setSnapshotIntervalSecs(int snapshotIntervalSecs) {
        this.snapshotIntervalSecs = snapshotIntervalSecs;
    }

    public static RaftConfigurationBuilder builder() {
        return new RaftConfigurationBuilder();
    }

    public static final class RaftConfigurationBuilder {
        private String cacheDir;
        private int electionTimeoutMs;
        private int snapshotIntervalSecs;

        private RaftConfigurationBuilder() {
        }

        public RaftConfigurationBuilder withCacheDir(String cacheDir) {
            this.cacheDir = cacheDir;
            return this;
        }

        public RaftConfigurationBuilder withElectionTimeoutMs(int electionTimeoutMs) {
            this.electionTimeoutMs = electionTimeoutMs;
            return this;
        }

        public RaftConfigurationBuilder withSnapshotIntervalSecs(int snapshotIntervalSecs) {
            this.snapshotIntervalSecs = snapshotIntervalSecs;
            return this;
        }

        public RaftConfiguration build() {
            RaftConfiguration raftConfiguration = new RaftConfiguration();
            raftConfiguration.setCacheDir(cacheDir);
            raftConfiguration.setElectionTimeoutMs(electionTimeoutMs);
            raftConfiguration.setSnapshotIntervalSecs(snapshotIntervalSecs);
            return raftConfiguration;
        }
    }
}
