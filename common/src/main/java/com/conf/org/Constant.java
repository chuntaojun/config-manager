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
package com.conf.org;

import java.util.Formatter;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class Constant {

	public static final String NEWLINE;

	static {
		String newLine;
		try (Formatter formatter = new Formatter()) {
			newLine = formatter.format("%n").toString();
		}
		catch (Exception e) {
			newLine = "\n";
		}
		NEWLINE = newLine;
	}

	public static final String CONFIG_MANAGER_SERVER_IP = "com.lessspring.org.config-manager.server.ip";

	/**
	 * 
	 */
	public static final String USE_ONLY_SITE_INTERFACES = "com.lessspring.org.config-manager.inetutils.use-only-site-local-interfaces";

	/**
	 * 
	 */
	public static final String PREFERRED_NETWORKS = "com.lessspring.org.config-manager.inetutils.preferred-networks";

	/**
	 * 
	 */
	public static final String IGNORED_INTERFACES = "com.lessspring.org.config-manager.inetutils.ignored-interfaces";

	/**
	 * 
	 */
	public static final String IP_ADDRESS = "com.lessspring.org.config-manager.inetutils.ip-address";

	/**
	 * 
	 */
	public static final String PREFER_HOSTNAME_OVER_IP = "com.lessspring.org.config-manager.inetutils.prefer-hostname-over-ip";

	/**
	 * 
	 */
	public static final String SYSTEM_PREFER_HOSTNAME_OVER_IP = "com.lessspring.org.config-manager.preferHostnameOverIp";

	/**
	 * 
	 */
	public static final String SHARE_ID_NAME = "share_id";

		/**
		 *
		 */
	public static final String ID = "id";
}
