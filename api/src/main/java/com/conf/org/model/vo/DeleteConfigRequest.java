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
package com.conf.org.model.vo;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class DeleteConfigRequest extends BaseConfigRequest {

	private boolean beta;

	public boolean isBeta() {
		return beta;
	}

	public void setBeta(boolean beta) {
		this.beta = beta;
	}

	public static DeleteConfigRequestBuilder sbuilder() {
		return new DeleteConfigRequestBuilder();
	}

	public static final class DeleteConfigRequestBuilder {
		private String dataId;
		private String groupId;
		private boolean beta;

		private DeleteConfigRequestBuilder() {
		}

		public DeleteConfigRequestBuilder dataId(String dataId) {
			this.dataId = dataId;
			return this;
		}

		public DeleteConfigRequestBuilder groupId(String groupId) {
			this.groupId = groupId;
			return this;
		}

		public DeleteConfigRequestBuilder beta(boolean beta) {
			this.beta = beta;
			return this;
		}

		public DeleteConfigRequest build() {
			DeleteConfigRequest deleteConfigRequest = new DeleteConfigRequest();
			deleteConfigRequest.setDataId(dataId);
			deleteConfigRequest.setGroupId(groupId);
			deleteConfigRequest.setBeta(beta);
			return deleteConfigRequest;
		}
	}
}
