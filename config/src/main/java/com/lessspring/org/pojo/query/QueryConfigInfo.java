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
package com.lessspring.org.pojo.query;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class QueryConfigInfo {

    private String namespaceId = "default";
    private String groupId = "DEFAULT_GROUP";
    private boolean beta = false;
    private String dataId;

    public QueryConfigInfo() {
    }

    public QueryConfigInfo(String dataId) {
        this.dataId = dataId;
    }

    public QueryConfigInfo(String groupId, String dataId) {
        this.groupId = groupId;
        this.dataId = dataId;
    }

    public QueryConfigInfo(String namespaceId, String groupId, String dataId) {
        this.namespaceId = namespaceId;
        this.groupId = groupId;
        this.dataId = dataId;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public boolean getBeta() {
        return beta;
    }

    public void setBeta(boolean beta) {
        this.beta = beta;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String namespaceId = "default";
        private String groupId = "DEFAULT_GROUP";
        private String dataId;
        private boolean beta = false;

        private Builder() {
        }

        public Builder namespaceId(String namespaceId) {
            if (StringUtils.isNotEmpty(namespaceId)) {
                this.namespaceId = namespaceId;
            }
            return this;
        }

        public Builder groupId(String groupId) {
            if (StringUtils.isNotEmpty(groupId)) {
                this.groupId = groupId;
            }
            return this;
        }

        public Builder dataId(String dataId) {
            this.dataId = dataId;
            return this;
        }

        public Builder beta(boolean beta) {
            this.beta = beta;
            return this;
        }

        public QueryConfigInfo build() {
            QueryConfigInfo queryConfigInfo = new QueryConfigInfo();
            queryConfigInfo.setNamespaceId(namespaceId);
            queryConfigInfo.setGroupId(groupId);
            queryConfigInfo.setDataId(dataId);
            queryConfigInfo.setBeta(beta);
            return queryConfigInfo;
        }
    }
}
