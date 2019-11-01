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
package com.lessspring.org.configuration;

import com.lessspring.org.utils.SystemEnv;
import com.lessspring.org.utils.WaitFinish;
import org.apache.commons.lang3.StringUtils;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Relevant system parameter is set in the Spring in the Environment
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@WaitFinish
public class ConfigEnvironmentProcessor implements EnvironmentPostProcessor {

	private final SystemEnv systemEnv = SystemEnv.getSingleton();

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment,
			SpringApplication application) {
		String dumpToFileKey = "com.lessspring.org.config-manager.dumpToFile";
		boolean dumpToFile = environment.getProperty(dumpToFileKey, Boolean.class, false);
		systemEnv.setDumpToFile(dumpToFile);

		String openWorkCostDisplayKey = "com.lessspring.org.config-manager.openWorkCostDisplay";
		boolean openWorkCostDisplay = environment.getProperty(openWorkCostDisplayKey,
				Boolean.class, false);
		systemEnv.setOpenWorkCostDisplay(openWorkCostDisplay);

		String emailHost = environment.getProperty(SystemEnv.EMAIL_HOST, "");
		String emailUsername = environment.getProperty(SystemEnv.EMAIL_USERNAME, "");
		String emailPwd = environment.getProperty(SystemEnv.EMAIL_PASSWORD, "");
		String emailSmtpAuth = environment.getProperty(SystemEnv.EMAIL_SMTP_AUTH,
				"false");
		String emailSmtpStarttlsEnable = environment
				.getProperty(SystemEnv.EMAIL_SMTP_STARTTLS_ENABLE, "false");
		String emailSmtpStarttlsRequired = environment
				.getProperty(SystemEnv.EMAIL_SMTP_STARTTLS_EQUIRED, "false");

		if (StringUtils.isNoneEmpty(emailHost, emailUsername, emailPwd)) {
			String semailHost = "spring.mail.host";
			environment.getSystemEnvironment().put(semailHost, emailHost);
			String semailUsername = "spring.mail.username";
			environment.getSystemEnvironment().put(semailUsername, emailUsername);
			String semailPwd = "spring.mail.password";
			environment.getSystemEnvironment().put(semailPwd, emailPwd);
			String semailSmtpAuth = "spring.mail.properties.mail.smtp.auth";
			environment.getSystemEnvironment().put(semailSmtpAuth, emailSmtpAuth);
			String semailSmtpStarttlsEnable = "spring.mail.properties.mail.smtp.starttls.enable";
			environment.getSystemEnvironment().put(semailSmtpStarttlsEnable,
					emailSmtpStarttlsEnable);
			String semailSmtpStarttlsRequired = "spring.mail.properties.mail.smtp.starttls.required";
			environment.getSystemEnvironment().put(semailSmtpStarttlsRequired,
					emailSmtpStarttlsRequired);
		}

		systemEnv.finishInit();
	}
}
