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
package com.lessspring.org.configuration.tps;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@ConfigurationProperties(prefix = "com.lessspring.org.config-manager.tps")
public class TpsSetting {

	private List<TpsResource> resources = new ArrayList<>();

	public List<TpsResource> getResources() {
		return resources;
	}

	public void setResources(List<TpsResource> resources) {
		this.resources = resources;
	}

	public static class TpsResource {

		private String resourceName;
		private Integer qps;

		public String getResourceName() {
			return resourceName;
		}

		public void setResourceName(String resourceName) {
			this.resourceName = resourceName;
		}

		public Integer getQps() {
			return qps;
		}

		public void setQps(Integer qps) {
			this.qps = qps;
		}
	}

}
