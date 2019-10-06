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

import com.lessspring.org.model.vo.DeleteConfigRequest;

import java.util.Map;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class DeleteConfigRequest4 extends DeleteConfigRequest {

    private Map<String, Object> attribute;

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
        private String dataId;
        private String groupId;
        private boolean beta;
        private Map<String, Object> attribute;

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

        public Builder attribute(Map<String, Object> attribute) {
            this.attribute = attribute;
            return this;
        }

        public DeleteConfigRequest4 build() {
            DeleteConfigRequest4 deleteConfigRequest4 = new DeleteConfigRequest4();
            deleteConfigRequest4.setDataId(dataId);
            deleteConfigRequest4.setGroupId(groupId);
            deleteConfigRequest4.setBeta(beta);
            deleteConfigRequest4.setAttribute(attribute);
            return deleteConfigRequest4;
        }
    }
}
