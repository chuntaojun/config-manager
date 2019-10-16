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

import com.lessspring.org.model.vo.PublishConfigRequest;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class PublishConfigRequest4 extends PublishConfigRequest {

	public static final String CLASS_NAME = PublishConfigRequest4.class
			.getCanonicalName();

	private String namespaceId;

	private Map<String, Object> attribute;

	public String getNamespaceId() {
		return namespaceId;
	}

	public void setNamespaceId(String namespaceId) {
		this.namespaceId = namespaceId;
	}

	public Map<String, Object> getAttribute() {
		return attribute;
	}

	public void setAttribute(Map<String, Object> attribute) {
		this.attribute = attribute;
	}

	public static Builder sBuilder() {
		return new Builder();
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

		public PublishConfigRequest4 build() {
			PublishConfigRequest4 publishConfigRequest4 = new PublishConfigRequest4();
			publishConfigRequest4.setDataId(dataId);
			publishConfigRequest4.setGroupId(groupId);
			publishConfigRequest4.setBeta(beta);
			publishConfigRequest4.setAttribute(attribute);
			publishConfigRequest4.setClientIps(clientIps);
			publishConfigRequest4.setContent(content);
			publishConfigRequest4.setType(type);
			publishConfigRequest4.setFile(file);
			publishConfigRequest4.setEncryption(encryption);
			publishConfigRequest4.setRequiresEncryption(requiresEncryption);
			publishConfigRequest4.setFile(this.isFile);
			publishConfigRequest4.setNamespaceId(namespaceId);
			return publishConfigRequest4;
		}
	}

	public static PublishConfigRequest4 copy(String namespaceId,
			PublishConfigRequest request) {
		return PublishConfigRequest4.sBuilder().namespaceId(namespaceId)
				.groupId(request.getGroupId()).dataId(request.getDataId())
				.beta(request.isBeta()).clientIps(request.getClientIps())
				.content(request.getContent()).type(request.getType())
				.file(request.getFile()).isFile(request.isFile())
				.encryption(request.getEncryption())
				.requiresEncryption(request.isRequiresEncryption()).build();
	}
}
