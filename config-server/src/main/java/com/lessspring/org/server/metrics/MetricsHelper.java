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

package com.lessspring.org.server.metrics;

import com.lessspring.org.server.utils.SpringUtils;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.lang.Nullable;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.function.ToDoubleFunction;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019/12/16 12:12 下午
 */
@Slf4j
public class MetricsHelper {

	public static <T> Gauge builderGauge(String name, @Nullable T obj,
			ToDoubleFunction<T> f) {
		return Gauge.builder(name, obj, f).register(SpringUtils.getBean(PrometheusMeterRegistry.class));
	}

	public static <T> Gauge builderGauge(String name, @Nullable T obj,
			ToDoubleFunction<T> f, Iterable<Tag> tags) {
		return Gauge.builder(name, obj, f).tags(tags).register(SpringUtils.getBean(PrometheusMeterRegistry.class));
	}

	public static <T> Gauge builderGauge(String name, @Nullable T obj,
			ToDoubleFunction<T> f, Iterable<Tag> tags, String description) {
		return Gauge.builder(name, obj, f).tags(tags).description(description)
				.register(SpringUtils.getBean(PrometheusMeterRegistry.class));
	}

	public static <T> Counter builderCounter(String name) {
		return Counter.builder(name).register(SpringUtils.getBean(PrometheusMeterRegistry.class));
	}

	public static <T> Counter builderCounter(String name, String description) {
		return Counter.builder(name).description(description).register(SpringUtils.getBean(PrometheusMeterRegistry.class));
	}

	public static Counter builderCounter(String name, Iterable<Tag> tags,
			String description) {
		return Counter.builder(name).tags(tags).description(description)
				.register(SpringUtils.getBean(PrometheusMeterRegistry.class));
	}

	public static DistributionSummary builderSummary(String name, String description) {
		return DistributionSummary.builder(name).description(description)
				.register(SpringUtils.getBean(PrometheusMeterRegistry.class));
	}

	public static DistributionSummary builderSummary(String name, Iterable<Tag> tags,
			String description) {
		return DistributionSummary.builder(name).tags(tags).description(description)
				.register(SpringUtils.getBean(PrometheusMeterRegistry.class));
	}
}
