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

package com.conf.org.server.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.springframework.boot.context.logging.LoggingApplicationListener.CONFIG_PROPERTY;
import static org.springframework.core.io.ResourceLoader.CLASSPATH_URL_PREFIX;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class LoggingSpringApplicationRunListener
		implements SpringApplicationRunListener, Ordered {

	private static final String DEFAULT_CONF_LOGBACK_LOCATION = CLASSPATH_URL_PREFIX
			+ "config-manager-logger.xml";

	private static final Logger logger = LoggerFactory
			.getLogger(LoggingSpringApplicationRunListener.class);

	private final SpringApplication application;

	private final String[] args;

	public LoggingSpringApplicationRunListener(SpringApplication application,
			String[] args) {
		this.application = application;
		this.args = args;
	}

	@Override
	public void starting() {

	}

	@Override
	public void environmentPrepared(ConfigurableEnvironment environment) {
		if (!environment.containsProperty(CONFIG_PROPERTY)) {
			System.setProperty(CONFIG_PROPERTY, DEFAULT_CONF_LOGBACK_LOCATION);
			if (logger.isInfoEnabled()) {
				logger.info(
						"There is no property named \"{}\" in Spring Boot Environment, "
								+ "and whose value is {} will be set into System's Properties",
						CONFIG_PROPERTY, DEFAULT_CONF_LOGBACK_LOCATION);
			}
		}
	}

	@Override
	public void contextPrepared(ConfigurableApplicationContext context) {

	}

	@Override
	public void contextLoaded(ConfigurableApplicationContext context) {

	}

	@Override
	public void started(ConfigurableApplicationContext context) {

	}

	@Override
	public void running(ConfigurableApplicationContext context) {

	}

	@Override
	public void failed(ConfigurableApplicationContext context, Throwable exception) {

	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}
}
