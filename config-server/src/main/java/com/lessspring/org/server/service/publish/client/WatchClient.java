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
package com.lessspring.org.server.service.publish.client;

import com.lessspring.org.server.service.publish.AbstractNotifyServiceImpl;
import org.springframework.http.server.reactive.ServerHttpResponse;

import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public abstract class WatchClient {

	protected String clientId;
	protected String clientIp;
	protected String namespaceId;
	protected Map<String, String> checkKey;
	protected ServerHttpResponse response;
	protected String watchType;
	private AbstractNotifyServiceImpl manager;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

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

	public boolean isChange(String key, String lastMd5) {
		return Objects.equals(lastMd5, checkKey.get(lastMd5));
	}

	public String getWatchType() {
		return watchType;
	}

	public void setWatchType(String watchType) {
		this.watchType = watchType;
	}

	public AbstractNotifyServiceImpl getManager() {
		return manager;
	}

	public void setManager(AbstractNotifyServiceImpl manager) {
		this.manager = manager;
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

	public void onClose() {
		manager.onWatchClientDeregister();
	}

	/**
	 * 监听配置的md5改变时触发回调
	 *
	 * @param key
	 * @param lastMd5
	 */
	public abstract void onChangeMd5(String key, String lastMd5);

}
