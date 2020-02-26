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
package com.conf.org.server.pojo.event.config;

import com.conf.org.event.EventType;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class NotifyEvent extends BaseEvent {

	public static final String TYPE = "NotifyEvent";

	public NotifyEvent() {
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public String label() {
		return TYPE;
	}

	public static final class Builder {
		private long sequence;
		private String namespaceId;
		private String dataId;
		private String groupId;
		private Object source;
		private boolean beta;
		private String clientIps;
		private EventType eventType;
		private String entryption;

		private Builder() {
		}

		public Builder sequence(long sequence) {
			this.sequence = sequence;
			return this;
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

		public Builder clientIps(String clientIps) {
			this.clientIps = clientIps;
			return this;
		}

		public Builder source(Object source) {
			this.source = source;
			return this;
		}

		public Builder beta(boolean beta) {
			this.beta = beta;
			return this;
		}

		public Builder entryption(String entryption) {
			this.entryption = entryption;
			return this;
		}

		public Builder eventType(EventType eventType) {
			this.eventType = eventType;
			return this;
		}

		public NotifyEvent build() {
			NotifyEvent notifyEvent = new NotifyEvent();
			notifyEvent.setSequence(sequence);
			notifyEvent.setNamespaceId(namespaceId);
			notifyEvent.setDataId(dataId);
			notifyEvent.setGroupId(groupId);
			notifyEvent.setSource(source);
			notifyEvent.setBeta(beta);
			notifyEvent.setClientIps(clientIps);
			notifyEvent.setEventType(eventType);
			notifyEvent.setEncryption(entryption);
			return notifyEvent;
		}
	}
}
