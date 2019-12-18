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
package com.lessspring.org.model.vo;

import com.lessspring.org.server.utils.GsonUtils;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class QueryConfigRequest extends BaseConfigRequest {

	public static QueryConfigRequestBuilder sbuilder() {
		return new QueryConfigRequestBuilder();
	}

	@Override
	public String toString() {
		return GsonUtils.toJson(this);
	}

	public static final class QueryConfigRequestBuilder {
		private String dataId;
		private String groupId;

		private QueryConfigRequestBuilder() {
		}

		public QueryConfigRequestBuilder dataId(String dataId) {
			this.dataId = dataId;
			return this;
		}

		public QueryConfigRequestBuilder groupId(String groupId) {
			this.groupId = groupId;
			return this;
		}

		public QueryConfigRequest build() {
			QueryConfigRequest queryConfigRequest = new QueryConfigRequest();
			queryConfigRequest.setDataId(dataId);
			queryConfigRequest.setGroupId(groupId);
			return queryConfigRequest;
		}
	}
}
