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
package com.lessspring.org.server.configuration.cluster;

import java.util.List;

import com.lessspring.org.raft.SnapshotOperate;
import com.lessspring.org.raft.TransactionIdManager;
import com.lessspring.org.server.service.cluster.ClusterManager;
import com.lessspring.org.server.service.distributed.BaseTransactionCommitCallback;
import com.lessspring.org.server.utils.PathConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Configuration
public class RaftNodeConfiguration {

	@Bean
	public ClusterManager clusterManager(
			@Autowired List<BaseTransactionCommitCallback> commitCallbacks,
			List<SnapshotOperate> snapshotOperates, TransactionIdManager transactionIdManager,
			PathConstants pathConstants) {
		return new ClusterManager(commitCallbacks, snapshotOperates, transactionIdManager,
				pathConstants);
	}

}
