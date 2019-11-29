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

import com.google.common.util.concurrent.RateLimiter;
import com.lessspring.org.observer.Occurrence;
import com.lessspring.org.observer.Publisher;
import com.lessspring.org.observer.Watcher;
import com.lessspring.org.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Configuration
public class TpsConfiguration {

	@Bean
	public TpsAnnotationProcessor tpsAnnotationProcessor(TpsManager tpsManager) {
		return new TpsAnnotationProcessor(tpsManager);
	}

	@Bean(value = "tpsSetting")
	public TpsSetting tpsSetting() {
		return new TpsSetting();
	}

	public static class TpsAnnotationProcessor
			implements BeanPostProcessor, Watcher<TpsSetting> {

		private final TpsManager tpsManager;
		@Autowired
		private TpsSetting tpsSetting;
		@Autowired
		private TpsAnnotationProcessor annotationProcessor;

		public TpsAnnotationProcessor(TpsManager tpsManager) {
			this.tpsManager = tpsManager;
		}

		@Override
		public Object postProcessBeforeInitialization(Object bean,
				String beanName) throws BeansException {
			final Map<String, Tuple2<Double, TpsSetting.TpsResource>> customer = new HashMap<>(
					8);
			for (TpsSetting.TpsResource resource : tpsSetting.getResources()) {
				Duration duration = Optional.ofNullable(resource.getDuration())
						.orElse(Duration.ofSeconds(1));
				Tuple2<Double, TpsSetting.TpsResource> tuple2 = Tuples
						.of(resource.getQps() * 1.0D / duration.getSeconds(), resource);
				customer.put(resource.getResourceName(), tuple2);
			}
			final Class<?> cls = AopUtils.getTargetClass(bean);
			if (cls.isAnnotationPresent(OpenTpsLimit.class)) {
				Stream.of(cls.getMethods()).forEach(method -> {
					LimitRule rule = method.getAnnotation(LimitRule.class);
					if (rule != null) {
						Supplier<TpsManager.LimitRuleEntry> limiterSupplier = () -> {
							Tuple2<Double, TpsSetting.TpsResource> tuple2 = customer
									.get(rule.resource());
							if (Objects.isNull(tuple2)) {
								TpsSetting.TpsResource tpsResource = new TpsSetting.TpsResource();
								tpsResource.setResourceName(rule.resource());
								tuple2 = Tuples.of(0D, tpsResource);
							}
							final double customerQps = tuple2.getT1();
							final TimeUnit unit = rule.timeUnit();
							// if qps == -1, Close the QPS current limit of the interface
							if (customerQps == -1) {
								return null;
							}
							double qps = customerQps == 0 ? rule.qps()
									: customerQps / unit.toSeconds(1);
							final TpsSetting.TpsResource resource = tuple2.getT2();
							resource.setQps(rule.qps());
							resource.setDuration(Duration.ofSeconds(unit.toSeconds(1)));
							log.info("[TpsResource] : {}", resource);
							tpsSetting.updateResource(resource);
							final RateLimiter limiter = RateLimiter.create(qps);
							Class<? extends FailStrategy> failStrategy = rule
									.failStrategy();
							TpsManager.LimitRuleEntry entry = tpsManager.query(rule.resource());
							if (entry == null) {
								entry = new TpsManager.LimitRuleEntry();
								try {
									FailStrategy strategy = failStrategy.newInstance();
									entry.setRateLimiter(limiter);
									entry.setStrategy(strategy);
									return entry;
								} catch (InstantiationException | IllegalAccessException e) {
									throw new RuntimeException(e);
								}
							} else {
								entry.setRateLimiter(limiter);
							}
							entry.setLimitRule(rule);
							return entry;
						};
						tpsManager.registerLimiter(rule.resource(), limiterSupplier);
					}
				});
			}
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean,
				String beanName) throws BeansException {
			return bean;
		}

		@Override
		public void onNotify(Occurrence<TpsSetting> occurrence, Publisher publisher) {
			tpsSetting = occurrence.getOrigin();
			String[] beanNames = SpringUtils.getBeanDefinitionNames();
			for (String beanName : beanNames) {
				annotationProcessor.postProcessBeforeInitialization(
						SpringUtils.getBean(beanName), beanName);
			}
		}

	}

}
