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
import lombok.NoArgsConstructor;

/**
 * config-info history
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigInfoHistoryDTO extends ConfigInfoDTO {

	public static final String NAME = "ConfigInfoHistoryDTO";
	private Long lastModifyTime;

	public static ConfigInfoHistoryDTOBuilder sbuilder() {
		return new ConfigInfoHistoryDTOBuilder();
	}

	public static final class ConfigInfoHistoryDTOBuilder {
		private Long id;
		private String namespaceId;
		private String groupId;
		private String dataId;
		private Boolean file;
		private byte[] fileSource;
		private byte[] content;
		private String type;
		private String encryption;
		private Long createTime;
		private Long lastModifyTime;

		private ConfigInfoHistoryDTOBuilder() {
		}

		public ConfigInfoHistoryDTOBuilder id(Long id) {
			this.id = id;
			return this;
		}

		public ConfigInfoHistoryDTOBuilder namespaceId(String namespaceId) {
			this.namespaceId = namespaceId;
			return this;
		}

		public ConfigInfoHistoryDTOBuilder groupId(String groupId) {
			this.groupId = groupId;
			return this;
		}

		public ConfigInfoHistoryDTOBuilder dataId(String dataId) {
			this.dataId = dataId;
			return this;
		}

		public ConfigInfoHistoryDTOBuilder file(Boolean file) {
			this.file = file;
			return this;
		}

		public ConfigInfoHistoryDTOBuilder fileSource(byte[] fileSource) {
			this.fileSource = fileSource;
			return this;
		}

		public ConfigInfoHistoryDTOBuilder content(byte[] content) {
			this.content = content;
			return this;
		}

		public ConfigInfoHistoryDTOBuilder type(String type) {
			this.type = type;
			return this;
		}

		public ConfigInfoHistoryDTOBuilder encryption(String encryption) {
			this.encryption = encryption;
			return this;
		}

		public ConfigInfoHistoryDTOBuilder createTime(Long createTime) {
			this.createTime = createTime;
			return this;
		}

		public ConfigInfoHistoryDTOBuilder lastModifyTime(Long lastModifyTime) {
			this.lastModifyTime = lastModifyTime;
			return this;
		}

		public ConfigInfoHistoryDTO build() {
			ConfigInfoHistoryDTO configInfoHistoryDTO = new ConfigInfoHistoryDTO();
			configInfoHistoryDTO.setId(id);
			configInfoHistoryDTO.setNamespaceId(namespaceId);
			configInfoHistoryDTO.setGroupId(groupId);
			configInfoHistoryDTO.setDataId(dataId);
			configInfoHistoryDTO.setFile(file);
			configInfoHistoryDTO.setFileSource(fileSource);
			configInfoHistoryDTO.setContent(content);
			configInfoHistoryDTO.setType(type);
			configInfoHistoryDTO.setEncryption(encryption);
			configInfoHistoryDTO.setCreateTime(createTime);
			configInfoHistoryDTO.setLastModifyTime(lastModifyTime);
			return configInfoHistoryDTO;
		}
	}
}
