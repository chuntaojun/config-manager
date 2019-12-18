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

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class BaseConfigRequest {

	private String dataId;
	private String groupId;
	@Expose
	private transient final Map<String, Object> attributes = new HashMap<>(4);

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String key) {
		return (T) attributes.get(key);
	}

	public synchronized BaseConfigRequest setAttribute(String key, Object value) {
		attributes.put(key, value);
		return this;
	}

	public static BaseConfigRequestBuilder builder() {
		return new BaseConfigRequestBuilder();
	}

	public static final class BaseConfigRequestBuilder {
		private String dataId;
		private String groupId;

		private BaseConfigRequestBuilder() {
		}

		public BaseConfigRequestBuilder withDataId(String dataId) {
			this.dataId = dataId;
			return this;
		}

		public BaseConfigRequestBuilder withGroupId(String groupId) {
			this.groupId = groupId;
			return this;
		}

		public BaseConfigRequest build() {
			BaseConfigRequest baseConfigRequest = new BaseConfigRequest();
			baseConfigRequest.setDataId(dataId);
			baseConfigRequest.setGroupId(groupId);
			return baseConfigRequest;
		}
	}
}
