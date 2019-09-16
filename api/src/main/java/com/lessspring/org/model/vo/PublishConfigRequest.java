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
public class PublishConfigRequest extends BaseConfigRequest {

    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String namespaceId;
        private String dataId;
        private String groupId;
        private String content;

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

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public PublishConfigRequest build() {
            PublishConfigRequest publishConfigRequest = new PublishConfigRequest();
            publishConfigRequest.setGroupId(this.groupId);
            publishConfigRequest.setContent(this.content);
            publishConfigRequest.setDataId(this.dataId);
            publishConfigRequest.setNamespaceId(this.namespaceId);
            return publishConfigRequest;
        }
    }
}
