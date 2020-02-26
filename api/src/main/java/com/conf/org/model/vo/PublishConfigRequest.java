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

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class PublishConfigRequest extends BaseConfigRequest {

	private boolean beta = false;
	private String clientIps;
	private String content;
	private String type = "text";
	private boolean isFile = false;
	private byte[] file;
	private String encryption = StringUtils.EMPTY;
	private Integer status = 0;
	private boolean requiresEncryption = false;

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

	public boolean isBeta() {
		return beta;
	}

	public void setBeta(boolean beta) {
		this.beta = beta;
	}

	public String getClientIps() {
		return clientIps;
	}

	public void setClientIps(String clientIps) {
		this.clientIps = clientIps;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getEncryption() {
		return encryption;
	}

	public void setEncryption(String encryption) {
		this.encryption = encryption;
	}

	public boolean isRequiresEncryption() {
		return requiresEncryption;
	}

	public void setRequiresEncryption(boolean requiresEncryption) {
		this.requiresEncryption = requiresEncryption;
	}

	public boolean isFile() {
		return isFile;
	}

	public void setFile(boolean file) {
		isFile = file;
	}

	public byte[] getFile() {
		return file;
	}

	public void setFile(byte[] file) {
		this.file = file;
	}

	public static PublishConfigRequestBuilder sbuilder() {
		return new PublishConfigRequestBuilder();
	}

	public static final class PublishConfigRequestBuilder {
		private String dataId;
		private String groupId;
		private boolean beta = false;
		private String clientIps;
		private String content;
		private String type;
		private boolean isFile = false;
		private byte[] file;
		private String encryption;
		private boolean requiresEncryption = false;

		private PublishConfigRequestBuilder() {
		}

		public PublishConfigRequestBuilder dataId(String dataId) {
			this.dataId = dataId;
			return this;
		}

		public PublishConfigRequestBuilder groupId(String groupId) {
			this.groupId = groupId;
			return this;
		}

		public PublishConfigRequestBuilder beta(boolean beta) {
			this.beta = beta;
			return this;
		}

		public PublishConfigRequestBuilder clientIps(String clientIps) {
			this.clientIps = clientIps;
			return this;
		}

		public PublishConfigRequestBuilder content(String content) {
			this.content = content;
			return this;
		}

		public PublishConfigRequestBuilder type(String type) {
			this.type = type;
			return this;
		}

		public PublishConfigRequestBuilder isFile(boolean isFile) {
			this.isFile = isFile;
			return this;
		}

		public PublishConfigRequestBuilder file(byte[] file) {
			this.file = file;
			return this;
		}

		public PublishConfigRequestBuilder encryption(String encryption) {
			this.encryption = encryption;
			return this;
		}

		public PublishConfigRequestBuilder requiresEncryption(
				boolean requiresEncryption) {
			this.requiresEncryption = requiresEncryption;
			return this;
		}

		public PublishConfigRequest build() {
			PublishConfigRequest publishConfigRequest = new PublishConfigRequest();
			publishConfigRequest.setDataId(dataId);
			publishConfigRequest.setGroupId(groupId);
			publishConfigRequest.setBeta(beta);
			publishConfigRequest.setClientIps(clientIps);
			publishConfigRequest.setContent(content);
			publishConfigRequest.setType(type);
			publishConfigRequest.setFile(file);
			publishConfigRequest.setEncryption(encryption);
			publishConfigRequest.setRequiresEncryption(requiresEncryption);
			publishConfigRequest.isFile = this.isFile;
			return publishConfigRequest;
		}
	}
}
