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
package com.conf.org.server.pojo.request;

import java.util.Map;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class PublishConfigHistory extends PublishConfigRequest4 {

	public static final String CLASS_NAME = PublishConfigHistory.class.getCanonicalName();

	private Long lastModifyTime;

	public static Builder hBuilder() {
		return new Builder();
	}

	public Long getLastModifyTime() {
		return lastModifyTime;
	}

	public void setLastModifyTime(Long lastModifyTime) {
		this.lastModifyTime = lastModifyTime;
	}

	public static final class Builder {
		private String namespaceId;
		private String dataId;
		private String groupId;
		private boolean beta = false;
		private Map<String, Object> attribute;
		private String clientIps;
		private String content;
		private String type;
		private boolean isFile = false;
		private byte[] file;
		private String encryption;
		private boolean requiresEncryption = false;
		private Long lastModifyTime;

		private Builder() {
		}

		public Builder namespaceId(String namespaceId) {
			this.namespaceId = namespaceId;
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

		public Builder beta(boolean beta) {
			this.beta = beta;
			return this;
		}

		public Builder attribute(Map<String, Object> attribute) {
			this.attribute = attribute;
			return this;
		}

		public Builder clientIps(String clientIps) {
			this.clientIps = clientIps;
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

		public Builder isFile(boolean isFile) {
			this.isFile = isFile;
			return this;
		}

		public Builder file(byte[] file) {
			this.file = file;
			return this;
		}

		public Builder encryption(String encryption) {
			this.encryption = encryption;
			return this;
		}

		public Builder requiresEncryption(boolean requiresEncryption) {
			this.requiresEncryption = requiresEncryption;
			return this;
		}

		public Builder lastModifyTime(Long lastModifyTime) {
			this.lastModifyTime = lastModifyTime;
			return this;
		}

		public PublishConfigHistory build() {
			PublishConfigHistory publishConfigHistory = new PublishConfigHistory();
			publishConfigHistory.setDataId(dataId);
			publishConfigHistory.setGroupId(groupId);
			publishConfigHistory.setBeta(beta);
			publishConfigHistory.setAttributes(attribute);
			publishConfigHistory.setClientIps(clientIps);
			publishConfigHistory.setContent(content);
			publishConfigHistory.setType(type);
			publishConfigHistory.setFile(file);
			publishConfigHistory.setEncryption(encryption);
			publishConfigHistory.setRequiresEncryption(requiresEncryption);
			publishConfigHistory.setFile(this.isFile);
			publishConfigHistory.setNamespaceId(namespaceId);
			publishConfigHistory.setLastModifyTime(lastModifyTime);
			return publishConfigHistory;
		}
	}

}
