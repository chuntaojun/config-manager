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
import com.lessspring.org.server.utils.ByteUtils;
import de.codecentric.boot.admin.server.config.AdminServerAutoConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerCloudFoundryAutoConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerHazelcastAutoConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration;
import org.springframework.boot.ResourceBanner;
import org.springframework.boot.actuate.autoconfigure.web.mappings.MappingsEndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.io.ByteArrayResource;

import java.util.Objects;
import java.util.Optional;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@SpringBootApplication(exclude = { AdminServerAutoConfiguration.class,
		AdminServerCloudFoundryAutoConfiguration.class,
		AdminServerNotifierAutoConfiguration.class,
		AdminServerHazelcastAutoConfiguration.class,
		ThymeleafAutoConfiguration.class,
		MappingsEndpointAutoConfiguration.class
})
public class ConfigApplication {

	public static void main(String[] args) {
		String openConfAdmin = Optional.ofNullable(System.getProperty("conf.admin")).orElse("false");
		if (Objects.equals(Boolean.TRUE.toString(), openConfAdmin)) {
				AdminServerApplication.main(args);
		}
		runApplication(args);
	}

	private static void runApplication(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder();
		builder.main(ConfigApplication.class).sources(ConfigApplication.class)
				.banner(new ResourceBanner(new ByteArrayResource(bannerText())))
				.run(args);
	}

	public static byte[] bannerText() {
		return ByteUtils.toBytes("_________               ___________\n"
				+ "\\_   ___ \\  ____   ____ \\_   _____/\n"
				+ "/    \\  \\/ /  _ \\ /    \\ |    __)  \n"
				+ "\\     \\___(  <_> )   |  \\|     \\   \n"
				+ " \\______  /\\____/|___|  /\\___  /   \n"
				+ "        \\/            \\/     \\/    ");
	}

}
