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

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class DeleteConfigRequest extends BaseConfigRequest {

    private boolean beta;

    public boolean isBeta() {
        return beta;
    }

    public void setBeta(boolean beta) {
        this.beta = beta;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        protected String dataId;
        protected String groupId;
        protected String encryption;
        protected boolean requiresEncryption = false;
        private boolean beta;

        private Builder() {
        }

        public Builder dataId(String dataId) {
            this.dataId = dataId;
            return this;
        }

        public Builder beta(boolean beta) {
            this.beta = beta;
            return this;
        }

        public Builder groupId(String groupId) {
            this.groupId = groupId;
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

        public DeleteConfigRequest build() {
            DeleteConfigRequest deleteConfigRequest = new DeleteConfigRequest();
            deleteConfigRequest.setDataId(dataId);
            deleteConfigRequest.setBeta(beta);
            deleteConfigRequest.setGroupId(groupId);
            return deleteConfigRequest;
        }
    }
}
