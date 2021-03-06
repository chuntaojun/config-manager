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
package com.lessspring.org.server.pojo.event.config;

import java.util.Arrays;

import com.lessspring.org.event.EventType;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class ConfigChangeEvent extends BaseEvent {

	public static final String TYPE = "ConfigChangeEvent";

	private String content;
	private byte[] fileSource;
	private String configType;
	private Long version = 0L;

	public static Builder builder() {
		return new Builder();
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getConfigType() {
		return configType;
	}

	public void setConfigType(String configType) {
		this.configType = configType;
	}

	public byte[] getFileSource() {
		return fileSource;
	}

	public void setFileSource(byte[] fileSource) {
		this.fileSource = fileSource;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	@Override
	public String label() {
		return TYPE;
	}

	@Override
	public String toString() {
		return "ConfigChangeEvent{" + "content='" + content + '\'' + ", fileSource="
				+ Arrays.toString(fileSource) + ", configType='" + configType + '\''
				+ ", version=" + version + '}';
	}

	public static final class Builder {
		private Object source;
		private String namespaceId;
		private String dataId;
		private String groupId;
		private String content;
		private EventType eventType;
		private String configType;
		private String encryption;
		private boolean file;
		private byte[] fileSource;
		private String clientIps;
		private boolean beta;
		private Long version = 0L;

		private Builder() {
		}

		public Builder source(Object source) {
			this.source = source;
			return this;
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

		public Builder content(String content) {
			this.content = content;
			return this;
		}

		public Builder eventType(EventType eventType) {
			this.eventType = eventType;
			return this;
		}

		public Builder configType(String configType) {
			this.configType = configType;
			return this;
		}

		public Builder encryption(String encryption) {
			this.encryption = encryption;
			return this;
		}

		public Builder beta(boolean beta) {
			this.beta = beta;
			return this;
		}

		public Builder file(boolean file) {
			this.file = file;
			return this;
		}

		public Builder fileSource(byte[] fileSource) {
			this.fileSource = fileSource;
			return this;
		}

		public Builder clientIps(String clientIps) {
			this.clientIps = clientIps;
			return this;
		}

		public Builder version(Long version) {
			this.version = version;
			return this;
		}

		public ConfigChangeEvent build() {
			ConfigChangeEvent configChangeEvent = new ConfigChangeEvent();
			configChangeEvent.setSource(source);
			configChangeEvent.setNamespaceId(namespaceId);
			configChangeEvent.setDataId(dataId);
			configChangeEvent.setGroupId(groupId);
			configChangeEvent.setContent(content);
			configChangeEvent.setEventType(eventType);
			configChangeEvent.setConfigType(configType);
			configChangeEvent.setEncryption(encryption);
			configChangeEvent.setFile(file);
			configChangeEvent.setBeta(beta);
			configChangeEvent.setFileSource(fileSource);
			configChangeEvent.setClientIps(clientIps);
			configChangeEvent.setVersion(version);
			return configChangeEvent;
		}
	}
}
