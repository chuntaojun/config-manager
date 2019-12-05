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
package com.lessspring.org;

import com.lessspring.org.constant.WatchType;
import lombok.Data;
import lombok.ToString;

import java.nio.file.Paths;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Data
@ToString
public class Configuration {

	private String namespaceId = "default";

	private String servers;

	private String cachePath = Paths
			.get(System.getProperty("user.home"), "config_manager_client").toString();

	private volatile String clientId;

	private String username;

	private String password;

	private String authToken;

	private boolean openHttps = false;

	private boolean localPref = false;

	private WatchType watchType = WatchType.SSE;

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String namespaceId = "default";
		private String servers;
		private String cachePath;
		private String clientId;
		private String username;
		private String password;
		private String authToken;
		private boolean openHttps = false;
		private boolean localPref = false;
		private WatchType watchType = WatchType.SSE;

		private Builder() {
		}

		public Builder namespaceId(String namespaceId) {
			this.namespaceId = namespaceId;
			return this;
		}

		public Builder servers(String servers) {
			this.servers = servers;
			return this;
		}

		public Builder cachePath(String cachePath) {
			this.cachePath = cachePath;
			return this;
		}

		public Builder clientId(String clientId) {
			this.clientId = clientId;
			return this;
		}

		public Builder username(String username) {
			this.username = username;
			return this;
		}

		public Builder password(String password) {
			this.password = password;
			return this;
		}

		public Builder authToken(String authToken) {
			this.authToken = authToken;
			return this;
		}

		public Builder openHttps(boolean openHttps) {
			this.openHttps = openHttps;
			return this;
		}

		public Builder localPref(boolean localPref) {
			this.localPref = localPref;
			return this;
		}

		public Builder watchType(WatchType watchType) {
			this.watchType = watchType;
			return this;
		}

		public Configuration build() {
			Configuration configuration = new Configuration();
			configuration.setNamespaceId(namespaceId);
			configuration.setServers(servers);
			configuration.setCachePath(cachePath);
			configuration.setClientId(clientId);
			configuration.setUsername(username);
			configuration.setPassword(password);
			configuration.setAuthToken(authToken);
			configuration.setOpenHttps(openHttps);
			configuration.setLocalPref(localPref);
			configuration.setWatchType(watchType);
			return configuration;
		}
	}
}
