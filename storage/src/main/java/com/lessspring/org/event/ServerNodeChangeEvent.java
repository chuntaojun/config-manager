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
package com.lessspring.org.event;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class ServerNodeChangeEvent extends BaseServerEvent {

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String nodeIp;
		private int nodePort;
		private EventType type;

		private Builder() {
		}

		public Builder nodeIp(String nodeIp) {
			this.nodeIp = nodeIp;
			return this;
		}

		public Builder nodePort(int nodePort) {
			this.nodePort = nodePort;
			return this;
		}

		public Builder type(EventType type) {
			this.type = type;
			return this;
		}

		public ServerNodeChangeEvent build() {
			ServerNodeChangeEvent serverNodeChangeEvent = new ServerNodeChangeEvent();
			serverNodeChangeEvent.setNodeIp(nodeIp);
			serverNodeChangeEvent.setNodePort(nodePort);
			serverNodeChangeEvent.setType(type);
			return serverNodeChangeEvent;
		}
	}
}
