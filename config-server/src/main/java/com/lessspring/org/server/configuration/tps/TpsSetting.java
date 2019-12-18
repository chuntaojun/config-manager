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
package com.lessspring.org.server.configuration.tps;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@ConfigurationProperties(prefix = "com.lessspring.org.config-manager.tps")
public class TpsSetting {

	private ArrayList<TpsResource> resources = new ArrayList<>();

	public List<TpsResource> getResources() {
		return resources;
	}

	public void setResources(ArrayList<TpsResource> resources) {
		this.resources = resources;
	}

	public void updateResource(TpsResource... _resources) {
		List<TpsResource> resourceList = Arrays.asList(_resources);
		resources.removeAll(resourceList);
		resources.addAll(resourceList);
	}

	public static class TpsResource {

		private String resourceName;
		private Double qps = 0.0D;
		private Duration duration = Duration.ofSeconds(1);

		public String getResourceName() {
			return resourceName;
		}

		public void setResourceName(String resourceName) {
			this.resourceName = resourceName;
		}

		public Double getQps() {
			return qps;
		}

		public void setQps(Double qps) {
			this.qps = qps;
		}

		public Duration getDuration() {
			return duration;
		}

		public void setDuration(Duration duration) {
			this.duration = duration;
		}

		@Override
		public String toString() {
			return "TpsResource{" + "resourceName='" + resourceName + '\'' + ", qps="
					+ qps + ", duration=" + duration + '}';
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof TpsResource) {
				TpsResource tr2 = (TpsResource) obj;
				return Objects.equals(resourceName, tr2.resourceName);
			}
			return false;
		}
	}

}
