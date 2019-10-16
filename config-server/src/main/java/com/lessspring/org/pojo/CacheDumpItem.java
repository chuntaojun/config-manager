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
package com.lessspring.org.pojo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.lessspring.org.model.dto.ConfigInfo;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class CacheDumpItem {

	private ConfigInfo configInfo;
	private boolean beta;
	private String clientIps;

	public ConfigInfo getConfigInfo() {
		return configInfo;
	}

	public void setConfigInfo(ConfigInfo configInfo) {
		this.configInfo = configInfo;
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

	public Set<String> allClientIps() {
		Set<String> clientIps = new HashSet<>();
		if (isBeta()) {
			for (String ip : getClientIps().split(",")) {
				if (Objects.equals("0.0.0.0:0", ip)) {
					return Collections.emptySet();
				}
				clientIps.add(ip.trim());
			}
		}
		return clientIps;
	}
}
