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
package com.lessspring.org.server.configuration;

import com.lessspring.org.server.aop.CurrentLimitActuator;
import com.lessspring.org.server.configuration.tps.TpsCondition;
import com.lessspring.org.server.configuration.tps.TpsManager;
import com.lessspring.org.server.service.common.EmailNotifyProperties;
import com.lessspring.org.server.utils.PathConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Configuration
public class CommonConfiguration {

	@Bean
	public PathConstants pathConstants() {
		return new PathConstants();
	}

	@Bean
	public EmailNotifyProperties emailNotifyProperties() {
		return new EmailNotifyProperties();
	}

	@Conditional(value = TpsCondition.class)
	@Bean
	public CurrentLimitActuator currentLimitActuator(@Autowired TpsManager tpsManager) {
		return new CurrentLimitActuator(tpsManager);
	}

}
