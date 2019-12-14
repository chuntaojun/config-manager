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
package com.lessspring.org.server;

import com.lessspring.org.admin.AdminServerApplication;
import de.codecentric.boot.admin.server.config.AdminServerAutoConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerCloudFoundryAutoConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerHazelcastAutoConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@SpringBootApplication(exclude = { AdminServerAutoConfiguration.class,
		AdminServerCloudFoundryAutoConfiguration.class,
		AdminServerNotifierAutoConfiguration.class,
		AdminServerHazelcastAutoConfiguration.class,
		SpringApplicationAdminJmxAutoConfiguration.class
})
public class ConfigApplication {

	public static void main(String[] args) {
		ConfigurableEnvironment environment = SpringApplication
				.run(ConfigApplication.class, args).getEnvironment();
		try {
			AdminServerApplication.injectEnvironment(environment);
			AdminServerApplication.main(args);
		}
		catch (Exception ignore) {

		}
	}

}
