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

import java.util.Objects;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class PublishConfigRequest extends BaseConfigRequest {

    private boolean beta = false;
    private String clientIps;
    private String content;
    private String type;
    private boolean isFile = false;
    private byte[] file;
    private String encryption;
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

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String dataId;
        private String groupId;
        private String encryption;
        private boolean requiresEncryption = false;
        private boolean beta = false;
        private String clientIps;
        private String content;
        private String type;
        private boolean isFile = false;
        private byte[] file;

        private Builder() {
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

        public Builder encryption(String encryption) {
            this.encryption = encryption;
            return this;
        }

        public Builder clientIps(String clientIps) {
            this.clientIps = clientIps;
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

        public Builder requiresEncryption(boolean requiresEncryption) {
            this.requiresEncryption = requiresEncryption;
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

        public PublishConfigRequest build() {
            PublishConfigRequest publishConfigRequest = new PublishConfigRequest();
            publishConfigRequest.setDataId(dataId);
            publishConfigRequest.setGroupId(groupId);
            publishConfigRequest.setBeta(beta);
            publishConfigRequest.setEncryption(encryption);
            publishConfigRequest.setClientIps(clientIps);
            publishConfigRequest.setRequiresEncryption(requiresEncryption);
            publishConfigRequest.setContent(content);
            publishConfigRequest.setType(type);
            if (isFile && StringUtils.isNotEmpty(content)) {
                throw new IllegalArgumentException("If set to the file type, do not set configuration information");
            }
            if (!isFile && (Objects.nonNull(file))) {
                throw new IllegalArgumentException("If the file type, file the source data");
            }
            return publishConfigRequest;
        }
    }
}
