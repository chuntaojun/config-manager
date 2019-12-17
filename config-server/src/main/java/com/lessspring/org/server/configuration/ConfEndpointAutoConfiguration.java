package com.lessspring.org.server.configuration;

import com.lessspring.org.server.configuration.http.ZConfMappingDescriptionProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.actuate.web.mappings.MappingsEndpoint;
import org.springframework.boot.actuate.web.mappings.servlet.FiltersMappingDescriptionProvider;
import org.springframework.boot.actuate.web.mappings.servlet.ServletsMappingDescriptionProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019/12/17 9:42 上午
 */
@Configuration
@EnableAutoConfiguration
public class ConfEndpointAutoConfiguration {

	@Bean
	@ConditionalOnEnabledEndpoint
	public MappingsEndpoint mappingsEndpoint(ApplicationContext applicationContext) {
		return new MappingsEndpoint(
				Collections.singleton(new ZConfMappingDescriptionProvider()),
				applicationContext);
	}

	@Configuration
	@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
	static class ServletWebConfiguration {

		@Bean
		ServletsMappingDescriptionProvider servletMappingDescriptionProvider() {
			return new ServletsMappingDescriptionProvider();
		}

		@Bean
		FiltersMappingDescriptionProvider filterMappingDescriptionProvider() {
			return new FiltersMappingDescriptionProvider();
		}

	}

}
