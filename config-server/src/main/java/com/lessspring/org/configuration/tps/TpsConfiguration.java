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

import java.lang.reflect.Method;
import java.util.function.Supplier;

import com.google.common.util.concurrent.RateLimiter;
import com.lessspring.org.tps.FailStrategy;
import com.lessspring.org.tps.LimitRule;
import com.lessspring.org.tps.OpenTpsLimit;
import com.lessspring.org.tps.TpsManager;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Configuration
public class TpsConfiguration {

	private final TpsManager tpsManager;

	public TpsConfiguration(TpsManager tpsManager) {
		this.tpsManager = tpsManager;
	}

	@Bean
	public TpsAnnotationProcessor tpsAnnotationProcessor() {
		return new TpsAnnotationProcessor();
	}

	private class TpsAnnotationProcessor implements BeanPostProcessor {

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName)
				throws BeansException {
			Class<?> cls = bean.getClass();
			if (cls.isAnnotationPresent(OpenTpsLimit.class)) {
				Method[] methods = cls.getMethods();
				for (Method method : methods) {
					LimitRule rule = method.getAnnotation(LimitRule.class);
					if (rule != null) {
						Supplier<TpsManager.LimitRuleEntry> limiterSupplier = () -> {
							RateLimiter limiter = RateLimiter
									.create(rule.qps());
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
		public Object postProcessAfterInitialization(Object bean, String beanName)
				throws BeansException {
			return bean;
		}
	}

}
