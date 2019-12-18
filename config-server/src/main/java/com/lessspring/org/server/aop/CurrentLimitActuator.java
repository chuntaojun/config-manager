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
package com.lessspring.org.server.aop;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.server.configuration.tps.LimitRule;
import com.lessspring.org.server.configuration.tps.OpenTpsLimit;
import com.lessspring.org.server.configuration.tps.TpsManager;
import com.lessspring.org.server.pojo.event.email.WarnEmailEvent;
import com.lessspring.org.server.service.common.EmailService;
import com.lessspring.org.server.utils.GsonUtils;
import com.lessspring.org.server.utils.PropertiesEnum;
import com.lessspring.org.server.utils.RenderUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

/**
 * Interface transaction current-limiting actuators
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */

@Component
@Aspect
public class CurrentLimitActuator implements PriorityOrdered {

	private final TpsManager tpsManager;

	@Autowired
	private EmailService emailService;

	private Map<String, LimitRule> methodCache = new ConcurrentHashMap<>();

	public CurrentLimitActuator(TpsManager tpsManager) {
		this.tpsManager = tpsManager;
	}

	@Pointcut("execution(* com.lessspring.org.server.handler..*.*(..))")
	public void urlHandler() {
	}

	@Around("urlHandler()")
	public Object aroundHttpHandler(ProceedingJoinPoint pjp) throws Throwable {
		String className = pjp.getTarget().getClass().getSimpleName();
		String methodName = pjp.getSignature().getName();
		String key = className + "-" + methodName;
		Class<?> classTarget = pjp.getTarget().getClass();
		if (classTarget.isAnnotationPresent(OpenTpsLimit.class)) {
			Class<?>[] par = ((MethodSignature) pjp.getSignature()).getParameterTypes();
			Method method = classTarget.getMethod(methodName, par);
			if (method.isAnnotationPresent(LimitRule.class)) {
				methodCache.computeIfAbsent(key,
						s -> method.getAnnotation(LimitRule.class));
				LimitRule rule = methodCache.get(key);
				TpsManager.LimitRuleEntry entry = tpsManager.query(rule.resource());
				final ResponseData<?> data = entry.tryAcquire();
				if (data == null) {
					return pjp.proceed();
				}
				final WarnEmailEvent emailEvent = new WarnEmailEvent(
						PropertiesEnum.EmailType.WARN);
				emailEvent.setMsg(GsonUtils.toJson(data));
				emailService.publishEmailEvent(emailEvent);
				return RenderUtils.render(Mono.just(data));
			}
		}
		return pjp.proceed();
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}
}
