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
package com.conf.org.server.utils;

import java.time.LocalDate;
import java.util.function.Supplier;

import com.conf.org.PathUtils;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class SystemEnv {

	public static final String EMAIL_HOST = "com.lessspring.org.config-manager.email.host";
	public static final String EMAIL_USERNAME = "com.lessspring.org.config-manager.email.username";
	public static final String EMAIL_PASSWORD = "com.lessspring.org.config-manager.email.password";
	public static final String EMAIL_SMTP_AUTH = "com.lessspring.org.config-manager.email.smtp.auth";
	public static final String EMAIL_SMTP_STARTTLS_ENABLE = "com.lessspring.org.config-manager.starttls.enable";
	public static final String EMAIL_SMTP_STARTTLS_EQUIRED = "com.lessspring.org.config-manager.email.starttls.required";

	private static final SystemEnv INSTANCE = new SystemEnv();
	public Supplier<String> jvmHeapDumpFileNameSuppiler = () -> PathUtils
			.finalPath("tmp/jvm", "config-manager-jvm-" + LocalDate.now() + ".hprof");
	private boolean inited = false;
	private boolean dumpToFile = true;
	private boolean openWorkCostDisplay = false;
	private String emailHost;
	private String emailName;
	private String emailPwd;
	private String emailSmtpAuth;
	private String emailSmtpStarttlsEnable;
	private String emailSmtpStarttlsEequired;

	private SystemEnv() {
	}

	public static SystemEnv getSingleton() {
		return INSTANCE;
	}

	public boolean isDumpToFile() {
		needFinishInited();
		return dumpToFile;
	}

	public void setDumpToFile(boolean dumpToFile) {
		this.dumpToFile = dumpToFile;
	}

	public boolean isOpenWorkCostDisplay() {
		needFinishInited();
		return openWorkCostDisplay;
	}

	public void setOpenWorkCostDisplay(boolean openWorkCostDisplay) {
		this.openWorkCostDisplay = openWorkCostDisplay;
	}

	public String getEmailHost() {
		needFinishInited();
		return emailHost;
	}

	public void setEmailHost(String emailHost) {
		this.emailHost = emailHost;
	}

	public String getEmailName() {
		needFinishInited();
		return emailName;
	}

	public void setEmailName(String emailName) {
		this.emailName = emailName;
	}

	public String getEmailPwd() {
		needFinishInited();
		return emailPwd;
	}

	public void setEmailPwd(String emailPwd) {
		this.emailPwd = emailPwd;
	}

	public String getEmailSmtpAuth() {
		needFinishInited();
		return emailSmtpAuth;
	}

	public void setEmailSmtpAuth(String emailSmtpAuth) {
		this.emailSmtpAuth = emailSmtpAuth;
	}

	public String getEmailSmtpStarttlsEnable() {
		needFinishInited();
		return emailSmtpStarttlsEnable;
	}

	public void setEmailSmtpStarttlsEnable(String emailSmtpStarttlsEnable) {
		this.emailSmtpStarttlsEnable = emailSmtpStarttlsEnable;
	}

	public String getEmailSmtpStarttlsEequired() {
		needFinishInited();
		return emailSmtpStarttlsEequired;
	}

	public void setEmailSmtpStarttlsEequired(String emailSmtpStarttlsEequired) {
		this.emailSmtpStarttlsEequired = emailSmtpStarttlsEequired;
	}

	public void finishInit() {
		inited = true;
	}

	private void needFinishInited() {
		if (!inited) {
			throw new IllegalStateException(
					"Initialize the unfinished system parameters");
		}
	}
}
