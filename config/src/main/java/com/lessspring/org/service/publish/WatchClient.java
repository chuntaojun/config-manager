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
package com.lessspring.org.service.publish;

import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.FluxSink;

import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class WatchClient {

    private String clientIp;
    private String namespaceId;
    private Map<String, String> checkKey;
    private FluxSink<?> sink;
    private ServerHttpResponse response;

    public String getClientIp() {
        return clientIp;
    }

    private void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    private void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public Map<String, String> getCheckKey() {
        return checkKey;
    }

    private void setCheckKey(Map<String, String> checkKey) {
        this.checkKey = checkKey;
    }

    public ServerHttpResponse getResponse() {
        return response;
    }

    private void setResponse(ServerHttpResponse response) {
        this.response = response;
    }

    public FluxSink getSink() {
        return sink;
    }

    private void setSink(FluxSink sink) {
        this.sink = sink;
    }

    public boolean isChange(String key, String lastMd5) {
        return Objects.equals(lastMd5, checkKey.get(lastMd5));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String clientIp;
        private String namespaceId;
        private Map<String, String> checkKey;
        private FluxSink sink;
        private ServerHttpResponse response;

        private Builder() {
        }

        public Builder clientIp(String clientIp) {
            this.clientIp = clientIp;
            return this;
        }

        public Builder namespaceId(String namespaceId) {
            this.namespaceId = namespaceId;
            return this;
        }

        public Builder checkKey(Map<String, String> checkKey) {
            this.checkKey = checkKey;
            return this;
        }

        public Builder sink(FluxSink sink) {
            this.sink = sink;
            return this;
        }

        public Builder response(ServerHttpResponse response) {
            this.response = response;
            return this;
        }

        public WatchClient build() {
            WatchClient watchClient = new WatchClient();
            watchClient.setClientIp(clientIp);
            watchClient.setNamespaceId(namespaceId);
            watchClient.setCheckKey(checkKey);
            watchClient.setSink(sink);
            watchClient.setResponse(response);
            return watchClient;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WatchClient client = (WatchClient) o;
        return Objects.equals(clientIp, client.clientIp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientIp);
    }
}
