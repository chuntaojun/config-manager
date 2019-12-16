package com.lessspring.org.server.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019/12/15 1:02 下午
 */
@Configuration
public class PageRouter {

	@Value("classpath:/static/config-manager-web/index.html")
	private Resource indexHtml;

	@Value("classpath:/static/config-manager-web/page-login.html")
	private Resource loginHtml;

	@Value("classpath:/static/config-manager-web/namespace_manager.html")
	private Resource namespaceHtml;

	@Value("classpath:/static/config-manager-web/node_manager.html")
	private Resource nodeHtml;

	@Value("classpath:/static/config-manager-web/user_manager.html")
	private Resource userHtml;

	@Value("classpath:/static/config-manager-web/config_manager.html")
	private Resource configHtml;

	@Value("classpath:/static/config-manager-web/config_watch_manager.html")
	private Resource configWatchHtml;

	@Bean(value = "pageRouterImpl")
	public RouterFunction<ServerResponse> notifyRouter() {
		return route(GET("/").and(accept(MediaType.ALL)),
				request -> ok().syncBody(loginHtml))
						.andRoute(GET("/page/index").and(accept(MediaType.ALL)),
								request -> ok().syncBody(indexHtml))
						.andRoute(GET("/page/node").and(accept(MediaType.ALL)),
								request -> ok().syncBody(nodeHtml))
						.andRoute(GET("/page/configWatch").and(accept(MediaType.ALL)),
								request -> ok().syncBody(configWatchHtml))
						.andRoute(GET("/page/namespace").and(accept(MediaType.ALL)),
								request -> ok().syncBody(namespaceHtml))
						.andRoute(GET("/page/user").and(accept(MediaType.ALL)),
								request -> ok().syncBody(userHtml))
						.andRoute(GET("/page/config").and(accept(MediaType.ALL)),
								request -> ok().syncBody(configHtml));
	}

	@Bean
	RouterFunction<ServerResponse> staticResourceRouter() {
		return RouterFunctions.resources("/**", new ClassPathResource("static/"));
	}

}
