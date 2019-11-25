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
import com.lessspring.org.tps.FailStrategy;
import com.lessspring.org.tps.LimitRule;
import com.lessspring.org.tps.OpenTpsLimit;
import com.lessspring.org.tps.TpsManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
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

	public static class TpsAnnotationProcessor implements BeanPostProcessor, Watcher<TpsSetting>, ApplicationContextAware {

		@Autowired
		private TpsSetting tpsSetting;

		@Autowired
		private TpsAnnotationProcessor annotationProcessor;

		private final TpsManager tpsManager;

		private ApplicationContext applicationContext;

		public TpsAnnotationProcessor(TpsManager tpsManager) {
			this.tpsManager = tpsManager;
		}

		@Override
		public Object postProcessBeforeInitialization(@NotNull Object bean,
				String beanName) throws BeansException {
			final Map<String, Double> customer = new HashMap<>(8);
			for (TpsSetting.TpsResource resource : tpsSetting.getResources()) {
				Duration duration = resource.getDuration();
				customer.put(resource.getResourceName(),
						resource.getQps() * 1.0D / duration.getSeconds());
			}
			Class<?> cls = bean.getClass();
			if (cls.isAnnotationPresent(OpenTpsLimit.class)) {
				Method[] methods = cls.getMethods();
				for (Method method : methods) {
					LimitRule rule = method.getAnnotation(LimitRule.class);
					if (rule != null) {
						Supplier<TpsManager.LimitRuleEntry> limiterSupplier = () -> {
							final Double customerQps = customer.get(rule.resource());
							final TimeUnit unit = rule.timeUnit();
							if (customerQps != null && customerQps == -1) {
								return null;
							}
							double qps = customerQps == null ? rule.qps()
									: customerQps / unit.toSeconds(1);
							final RateLimiter limiter = RateLimiter.create(qps);
							Class<? extends FailStrategy> failStrategy = rule
									.failStrategy();
							try {
								FailStrategy strategy = failStrategy.newInstance();
								TpsManager.LimitRuleEntry entry = new TpsManager.LimitRuleEntry();
								entry.setLimitRule(rule);
								entry.setRateLimiter(limiter);
								entry.setStrategy(strategy);
								return entry;
							}
							catch (InstantiationException | IllegalAccessException e) {
								throw new RuntimeException(e);
							}
						};
						tpsManager.registerLimiter(rule.resource(), limiterSupplier);
					}
				}
			}
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(@NotNull Object bean,
				String beanName) throws BeansException {
			return bean;
		}

		@Override
		public void onNotify(Occurrence<TpsSetting> occurrence, Publisher publisher) {
			tpsSetting = occurrence.getOrigin();
			String[] beanNames = applicationContext.getBeanDefinitionNames();
			for (String beanName : beanNames) {
				annotationProcessor.postProcessBeforeInitialization(
						applicationContext.getBean(beanName), beanName);
			}
		}

		@Override
		public void setApplicationContext(@NotNull ApplicationContext applicationContext)
				throws BeansException {
			this.applicationContext = applicationContext;
		}
	}

}
