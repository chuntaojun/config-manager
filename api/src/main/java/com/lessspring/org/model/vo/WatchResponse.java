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

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class WatchResponse {

	private String groupId;
	private String dataId;
	private String content;
	private byte[] file;
	private String encryption;
	private String type;

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public byte[] getFile() {
		return file;
	}

	public void setFile(byte[] file) {
		this.file = file;
	}

	public String getEncryption() {
		return encryption;
	}

	public void setEncryption(String encryption) {
		this.encryption = encryption;
	}

	public boolean isEmpty() {
		return StringUtils.isAllEmpty(groupId, dataId, content, type);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String groupId;
		private String dataId;
		private String content;
		private String type;
		private byte[] file;
		private String encryption;

		private Builder() {
		}

		public Builder groupId(String groupId) {
			this.groupId = groupId;
			return this;
		}

		public Builder dataId(String dataId) {
			this.dataId = dataId;
			return this;
		}

		public Builder content(String content) {
			this.content = content;
			return this;
		}

		public Builder type(String type) {
			this.type = type;
			return this;
		}

		public Builder encryption(String encryption) {
			this.encryption = encryption;
			return this;
		}

		public Builder file(byte[] file) {
			this.file = file;
			return this;
		}

		public WatchResponse build() {
			WatchResponse watchResponse = new WatchResponse();
			watchResponse.setGroupId(groupId);
			watchResponse.setDataId(dataId);
			watchResponse.setContent(content);
			watchResponse.setType(type);
			watchResponse.setFile(file);
			watchResponse.setEncryption(encryption);
			return watchResponse;
		}
	}

	@Override
	public String toString() {
		return "WatchResponse{" + "groupId='" + groupId + '\'' + ", dataId='" + dataId
				+ '\'' + ", content='" + content + '\'' + ", type='" + type + '\'' + '}';
	}
}
