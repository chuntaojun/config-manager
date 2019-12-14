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
package com.lessspring.org.server.pojo.event.config;

import com.lessspring.org.event.EventType;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public abstract class BaseEvent {

	private final long createTime = System.currentTimeMillis();
	private long sequence;
	private String namespaceId = "default";
	private String dataId;
	private String groupId = "DEFAULT_GROUP";
	private String clientIps = "";
	private boolean beta;
	private boolean isFile = false;
	private String encryption;
	private Object source;
	private EventType eventType;

	public long getSequence() {
		return sequence;
	}

	public void setSequence(long sequence) {
		this.sequence = sequence;
	}

	public String getNamespaceId() {
		return namespaceId;
	}

	public void setNamespaceId(String namespaceId) {
		this.namespaceId = namespaceId;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public Object getSource() {
		return source;
	}

	public void setSource(Object source) {
		this.source = source;
	}

	public long getCreateTime() {
		return createTime;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public String getEncryption() {
		return encryption;
	}

	public void setEncryption(String encryption) {
		this.encryption = encryption;
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

	public boolean isFile() {
		return isFile;
	}

	public void setFile(boolean file) {
		isFile = file;
	}

	public abstract String label();
}
