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
package com.lessspring.org.db.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ConfigBetaInfoDTO extends ConfigInfoDTO {

	private String clientIps;

	public static ConfigBetaInfoDTOBuilder sbuilder() {
		return new ConfigBetaInfoDTOBuilder();
	}

	public static final class ConfigBetaInfoDTOBuilder {
		private String clientIps;
		private Long id;
		private String namespaceId;
		private String groupId;
		private String dataId;
		private byte[] fileSource;
		private byte[] content;
		private String type;
		private String encryption = "";
		private Long createTime;
		private Long version = 0L;

		private ConfigBetaInfoDTOBuilder() {
		}

		public ConfigBetaInfoDTOBuilder clientIps(String clientIps) {
			this.clientIps = clientIps;
			return this;
		}

		public ConfigBetaInfoDTOBuilder id(Long id) {
			this.id = id;
			return this;
		}

		public ConfigBetaInfoDTOBuilder namespaceId(String namespaceId) {
			this.namespaceId = namespaceId;
			return this;
		}

		public ConfigBetaInfoDTOBuilder groupId(String groupId) {
			this.groupId = groupId;
			return this;
		}

		public ConfigBetaInfoDTOBuilder dataId(String dataId) {
			this.dataId = dataId;
			return this;
		}

		public ConfigBetaInfoDTOBuilder fileSource(byte[] fileSource) {
			this.fileSource = fileSource;
			return this;
		}

		public ConfigBetaInfoDTOBuilder content(byte[] content) {
			this.content = content;
			return this;
		}

		public ConfigBetaInfoDTOBuilder type(String type) {
			this.type = type;
			return this;
		}

		public ConfigBetaInfoDTOBuilder encryption(String encryption) {
			this.encryption = encryption;
			return this;
		}

		public ConfigBetaInfoDTOBuilder createTime(Long createTime) {
			this.createTime = createTime;
			return this;
		}

		public ConfigBetaInfoDTOBuilder version(Long version) {
			this.version = version;
			return this;
		}

		public ConfigBetaInfoDTO build() {
			ConfigBetaInfoDTO configBetaInfoDTO = new ConfigBetaInfoDTO();
			configBetaInfoDTO.setClientIps(clientIps);
			configBetaInfoDTO.setId(id);
			configBetaInfoDTO.setNamespaceId(namespaceId);
			configBetaInfoDTO.setGroupId(groupId);
			configBetaInfoDTO.setDataId(dataId);
			configBetaInfoDTO.setFileSource(fileSource);
			configBetaInfoDTO.setContent(content);
			configBetaInfoDTO.setType(type);
			configBetaInfoDTO.setEncryption(encryption);
			configBetaInfoDTO.setCreateTime(createTime);
			configBetaInfoDTO.setVersion(version);
			return configBetaInfoDTO;
		}
	}
}
