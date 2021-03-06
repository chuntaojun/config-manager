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
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigInfoDTO {

	public static final String NAME = "ConfigInfoDTO";

	private Long id;
	private String namespaceId;
	private String groupId;
	private String dataId;
	private byte[] fileSource;
	private byte[] content;
	private String type;
	private String remark;
	private String encryption = "";
	private Long createTime;
	private Integer status = 0;
	private Long version = 0L;

	public static ConfigInfoDTOBuilder builder() {
		return new ConfigInfoDTOBuilder();
	}

	public static final class ConfigInfoDTOBuilder {
		private Long id;
		private String namespaceId;
		private String groupId;
		private String dataId;
		private byte[] fileSource;
		private byte[] content;
		private String type;
		private String remark;
		private String encryption;
		private Long createTime;
		private Integer status = 0;
		private Long version = 0L;

		private ConfigInfoDTOBuilder() {
		}

		public ConfigInfoDTOBuilder id(Long id) {
			this.id = id;
			return this;
		}

		public ConfigInfoDTOBuilder namespaceId(String namespaceId) {
			this.namespaceId = namespaceId;
			return this;
		}

		public ConfigInfoDTOBuilder groupId(String groupId) {
			this.groupId = groupId;
			return this;
		}

		public ConfigInfoDTOBuilder dataId(String dataId) {
			this.dataId = dataId;
			return this;
		}

		public ConfigInfoDTOBuilder fileSource(byte[] fileSource) {
			this.fileSource = fileSource;
			return this;
		}

		public ConfigInfoDTOBuilder content(byte[] content) {
			this.content = content;
			return this;
		}

		public ConfigInfoDTOBuilder type(String type) {
			this.type = type;
			return this;
		}

		public ConfigInfoDTOBuilder encryption(String encryption) {
			this.encryption = encryption;
			return this;
		}

		public ConfigInfoDTOBuilder remark(String remark) {
			this.remark = remark;
			return this;
		}

		public ConfigInfoDTOBuilder createTime(Long createTime) {
			this.createTime = createTime;
			return this;
		}

		public ConfigInfoDTOBuilder status(Integer status) {
			this.status = status;
			return this;
		}

		public ConfigInfoDTOBuilder version(Long version) {
			this.version = version;
			return this;
		}

		public ConfigInfoDTO build() {
			ConfigInfoDTO configInfoDTO = new ConfigInfoDTO();
			configInfoDTO.setId(id);
			configInfoDTO.setRemark(remark);
			configInfoDTO.setNamespaceId(namespaceId);
			configInfoDTO.setGroupId(groupId);
			configInfoDTO.setDataId(dataId);
			configInfoDTO.setFileSource(fileSource);
			configInfoDTO.setContent(content);
			configInfoDTO.setType(type);
			configInfoDTO.setEncryption(encryption);
			configInfoDTO.setCreateTime(createTime);
			configInfoDTO.setVersion(version);
			configInfoDTO.setStatus(status);
			return configInfoDTO;
		}
	}
}
