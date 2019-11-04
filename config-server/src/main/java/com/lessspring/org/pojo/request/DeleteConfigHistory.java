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
package com.lessspring.org.pojo.request;

import java.util.Map;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class DeleteConfigHistory extends BaseRequest4 {

	public static final String CLASS_NAME = DeleteConfigHistory.class.getCanonicalName();

	private String namespaceId = "default";
	private Long lastModifyTime;

	public String getNamespaceId() {
		return namespaceId;
	}

	public void setNamespaceId(String namesapceId) {
		this.namespaceId = namesapceId;
	}

	public Long getLastModifyTime() {
		return lastModifyTime;
	}

	public void setLastModifyTime(Long lastModifyTime) {
		this.lastModifyTime = lastModifyTime;
	}

	public static final class Builder {
		private String namesapceId = "default";
		private String dataId;
		private String groupId;
		private Long lastModifyTime;
		private Map<String, Object> attribute;

		private Builder() {
		}

		public Builder namespaceId(String namesapceId) {
			this.namesapceId = namesapceId;
			return this;
		}

		public Builder dataId(String dataId) {
			this.dataId = dataId;
			return this;
		}

		public Builder groupId(String groupId) {
			this.groupId = groupId;
			return this;
		}

		public Builder lastModifyTime(Long lastModifyTime) {
			this.lastModifyTime = lastModifyTime;
			return this;
		}

		public Builder attribute(Map<String, Object> attribute) {
			this.attribute = attribute;
			return this;
		}

		public DeleteConfigHistory build() {
			DeleteConfigHistory deleteConfigHistory = new DeleteConfigHistory();
			deleteConfigHistory.setNamespaceId(namesapceId);
			deleteConfigHistory.setDataId(dataId);
			deleteConfigHistory.setGroupId(groupId);
			deleteConfigHistory.setLastModifyTime(lastModifyTime);
			deleteConfigHistory.setAttribute(attribute);
			return deleteConfigHistory;
		}
	}

}
