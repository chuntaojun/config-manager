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
package com.lessspring.org.model.dto;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class ConfigInfo {

    private String groupId;
    private String dataId;
    private String content;
    private String type;
    private byte[] file;
    private String encryption;

    public ConfigInfo() {
    }

    public ConfigInfo(String groupId, String dataId, String content, String type, String encryption) {
        this.groupId = groupId;
        this.dataId = dataId;
        this.content = content;
        this.type = type;
        this.encryption = encryption;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public void setEncryption(String encryption) {
        this.encryption = encryption;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getDataId() {
        return dataId;
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public String getEncryption() {
        return encryption;
    }

    public byte[] getFile() {
        return file;
    }

    public boolean isFile() {
        return file != null;
    }

    public byte[] getBytes() {
        if (isFile()) {
            return file;
        }
        return content.getBytes(Charset.forName(StandardCharsets.UTF_8.name()));
    }

    @Override
    public String toString() {
        return "ConfigInfo{" +
                "groupId='" + groupId + '\'' +
                ", dataId='" + dataId + '\'' +
                ", content='" + content + '\'' +
                ", type='" + type + '\'' +
                '}';
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

        public Builder file(byte[] file) {
            this.file = file;
            return this;
        }

        public Builder encryption(String encryption) {
            this.encryption = encryption;
            return this;
        }

        public ConfigInfo build() {
            ConfigInfo configInfo = new ConfigInfo();
            configInfo.setGroupId(groupId);
            configInfo.setDataId(dataId);
            configInfo.setContent(content);
            configInfo.setType(type);
            configInfo.setEncryption(encryption);
            return configInfo;
        }
    }
}
