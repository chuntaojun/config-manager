package com.lessspring.org.admin;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019/12/14 4:56 下午
 */
@EnableAdminServer
@SpringBootApplication
public class AdminServerApplication {

	private static ConfigurableEnvironment environment;

	public static void main(String[] args) {
		Properties webServer = new Properties();
		webServer.put("server.port", 2595);
		webServer.put("spring.jmx.default-domain", "ConFApplication");
		environment.getPropertySources()
				.addFirst(new PropertiesPropertySource("spring.admin", webServer));

		SpringApplicationBuilder builder = new SpringApplicationBuilder();
		builder.main(AdminServerApplication.class)
				.sources(AdminServerApplication.class)
				.environment(environment);
		builder.run(args);

	}

	public static void injectEnvironment(ConfigurableEnvironment environment) {
		AdminServerApplication.environment = environment;
	}

}
