package com.conf.org.admin;

import com.conf.org.utils.ByteUtils;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.ResourceBanner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ByteArrayResource;

import java.util.Properties;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019/12/14 4:56 下午
 */
@EnableAdminServer
@SpringBootApplication(exclude = SpringApplicationAdminJmxAutoConfiguration.class)
public class AdminServerApplication {

	public static void run(String[] args) {
		ConfigurableEnvironment environment = new StandardEnvironment();
		Properties webServer = new Properties();
		webServer.put("server.port", 2595);
		webServer.put("spring.jmx.default-domain", "ConFAdminApplication");
		environment.getPropertySources()
				.addFirst(new PropertiesPropertySource("spring.admin", webServer));

		SpringApplicationBuilder builder = new SpringApplicationBuilder();
		builder.main(AdminServerApplication.class).sources(AdminServerApplication.class)
				.banner(new ResourceBanner(new ByteArrayResource(bannerText())))
				.environment(environment);
		builder.run(args);
	}

	public static byte[] bannerText() {
		return ByteUtils.toBytes(
				"_________               ________________       .___      .__        \n"
						+ "\\_   ___ \\  ____   ____ \\_   _____/  _  \\    __| _/_____ |__| ____  \n"
						+ "/    \\  \\/ /  _ \\ /    \\ |    __)/  /_\\  \\  / __ |/     \\|  |/    \\ \n"
						+ "\\     \\___(  <_> )   |  \\|     \\/    |    \\/ /_/ |  Y Y  \\  |   |  \\\n"
						+ " \\______  /\\____/|___|  /\\___  /\\____|__  /\\____ |__|_|  /__|___|  /\n"
						+ "        \\/            \\/     \\/         \\/      \\/     \\/        \\/ ");
	}

}
