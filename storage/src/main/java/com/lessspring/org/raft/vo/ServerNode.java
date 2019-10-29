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
package com.lessspring.org.raft.vo;

import java.util.Objects;

import com.lessspring.org.raft.utils.ServerStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Data
@NoArgsConstructor
public class ServerNode {

	private String nodeIp;
	private int port;
	private String role = "Candidate";
	private String key = "";
	private ServerStatus serverStatus = ServerStatus.HEALTH;

	public String getKey() {
		if (StringUtils.isEmpty(key)) {
			key = nodeIp + ":" + port;
		}
		return key;
	}

	public String getNodeIp() {
		return nodeIp;
	}

	public void setNodeIp(String nodeIp) {
		this.nodeIp = nodeIp;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public ServerStatus getServerStatus() {
		return serverStatus;
	}

	public void setServerStatus(ServerStatus serverStatus) {
		this.serverStatus = serverStatus;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ServerNode node = (ServerNode) o;
		return port == node.port && Objects.equals(nodeIp, node.nodeIp)
				&& Objects.equals(key, node.key);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nodeIp, port, key);
	}

	public static ServerNodeBuilder builder() {
		return new ServerNodeBuilder();
	}

	public static final class ServerNodeBuilder {
		private String nodeIp;
		private int port;
		private String role;
		private ServerStatus serverStatus = ServerStatus.HEALTH;

		private ServerNodeBuilder() {
		}

		public ServerNodeBuilder nodeIp(String nodeIp) {
			this.nodeIp = nodeIp;
			return this;
		}

		public ServerNodeBuilder port(int port) {
			this.port = port;
			return this;
		}

		public ServerNodeBuilder role(String role) {
			this.role = role;
			return this;
		}

		public ServerNodeBuilder serverStatus(ServerStatus serverStatus) {
			this.serverStatus = serverStatus;
			return this;
		}

		public ServerNode build() {
			ServerNode serverNode = new ServerNode();
			serverNode.setNodeIp(nodeIp);
			serverNode.setPort(port);
			serverNode.setRole(role);
			serverNode.setServerStatus(serverStatus);
			return serverNode;
		}
	}
}
