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
package com.lessspring.org.aop;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.lessspring.org.configuration.security.NeedAuth;
import com.lessspring.org.exception.AuthForbidException;
import com.lessspring.org.pojo.Privilege;
import com.lessspring.org.service.security.AuthorityProcessor;
import com.lessspring.org.utils.PropertiesEnum;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

/**
 * Authority inspection actuators
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Aspect
@Component
public class AuthOperationActuator {

	private final Map<String, Optional<NeedAuth>> cache = new ConcurrentHashMap<>(8);

	private final AuthorityProcessor authorityProcessor;

	@Value("${com.lessspring.org.config-manager.environment}")
	private String developEnv;

	public AuthOperationActuator(AuthorityProcessor authorityProcessor) {
		this.authorityProcessor = authorityProcessor;
	}

	@Pointcut(value = "@annotation(com.lessspring.org.configuration.security.NeedAuth)")
	private void auth() {
	}

	@Around("auth()")
	public Object aroundService(ProceedingJoinPoint pjp) throws Throwable {
		if (!Objects.equals(developEnv, "develop")) {
			String methodName = pjp.getSignature().getName();
			Class<?> classTarget = pjp.getTarget().getClass();
			cache.computeIfAbsent(methodName, s -> {
				NeedAuth needAuth = null;
				try {
					Method method = classTarget.getMethod(methodName);
					if (method.isAnnotationPresent(NeedAuth.class)) {
						needAuth = method.getDeclaredAnnotation(NeedAuth.class);
					}
				}
				catch (NoSuchMethodException ignore) {
				}
				return Optional.ofNullable(needAuth);
			});
			Optional<NeedAuth> optionalNeedAuth = cache.getOrDefault(methodName,
					Optional.empty());
			optionalNeedAuth.ifPresent(authMethod -> {
				ServerRequest request = (ServerRequest) pjp.getArgs()[0];
				boolean[] throwables = new boolean[] { false };
				request.exchange().getSession().subscribe(webSession -> {
					String namespaceId = request.queryParam(authMethod.argueName())
							.orElse("default");
					Privilege privilege = webSession.getAttribute("privilege");
					log.info("privilege info : {}", privilege);
					if (Objects.isNull(privilege)
							|| !authorityProcessor.hasAuth(privilege, namespaceId)) {
						log.error(
								"No permission to access this resource, target namespaceId : {}, owner namespaceId : {}, "
										+ "role : {}",
								namespaceId,
								privilege == null ? Collections.emptyList()
										: privilege.getOwnerNamespaces(),
								privilege == null ? PropertiesEnum.Role.CUSTOMER
										: privilege.getRole());
						throwables[0] = true;
					}
				});
				if (throwables[0]) {
					throw new AuthForbidException(
							"No permission to access this resource");
				}
			});
		}
		return pjp.proceed();
	}

}
