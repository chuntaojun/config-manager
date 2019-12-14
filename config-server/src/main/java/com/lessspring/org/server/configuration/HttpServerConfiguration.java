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
package com.lessspring.org.server.configuration;

import java.util.Collections;
import java.util.Objects;

import com.google.gson.GsonBuilder;
import com.lessspring.org.executor.NameThreadFactory;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import reactor.netty.http.server.HttpServer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Configuration
public class HttpServerConfiguration {

	private final Environment environment;
	@Value("${com.lessspring.org.config-manager.netty.loopThreads}")
	private int loopThreads;
	@Value("${com.lessspring.org.config-manager.netty.workerThreads}")
	private int workerThreads;
	@Value("${server.port}")
	private int serverPort;

	public HttpServerConfiguration(Environment environment) {
		this.environment = environment;
	}

	@Bean
	public HttpServer httpServerForService(
			@Qualifier(value = "configRouterImpl") RouterFunction<ServerResponse> routerFunction) {
		return getHttpServer(routerFunction);
	}

	@Bean
	public HttpMessageConverters httpMessageConverters() {
		GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
		gsonHttpMessageConverter.setGson(new GsonBuilder().create());
		return new HttpMessageConverters(true,
				Collections.singletonList(gsonHttpMessageConverter));
	}

	/**
	 * <pre>
	 * 抽取的函数, 实现具体的 HttpServer 部署实现工作
	 * </pre>
	 * 
	 * @param routerFunction 路由实例
	 * @return {@link HttpServer}
	 */
	private HttpServer getHttpServer(RouterFunction<ServerResponse> routerFunction) {
		HttpHandler handler = RouterFunctions.toHttpHandler(routerFunction);
		ReactorHttpHandlerAdapter httpHandlerAdapter = new ReactorHttpHandlerAdapter(
				handler);
		HttpServer httpServer = HttpServer.create().host("localhost")
				.port(Integer.parseInt(
						Objects.requireNonNull(environment.getProperty("server.port"))));
		httpServer = httpServer
				.tcpConfiguration(tcpServer -> tcpServer.bootstrap(serverBootstrap -> {
					EventLoopGroup core = new KQueueEventLoopGroup(loopThreads,
							new NameThreadFactory("KQueue-Loop-"));
					EventLoopGroup worker = new KQueueEventLoopGroup(workerThreads,
							new NameThreadFactory("KQueue-Worker-"));
					serverBootstrap.group(core, worker);
					serverBootstrap.channel(NioServerSocketChannel.class)
							.option(ChannelOption.ALLOCATOR,
									PooledByteBufAllocator.DEFAULT)
							.option(ChannelOption.SO_REUSEADDR, true)
							.option(ChannelOption.SO_BACKLOG, 1000)
							.childOption(ChannelOption.ALLOCATOR,
									PooledByteBufAllocator.DEFAULT)
							.childOption(ChannelOption.SO_RCVBUF, 1024 * 1024)
							.childOption(ChannelOption.SO_SNDBUF, 1024 * 1024)
							.childOption(ChannelOption.AUTO_READ, false)
							.childOption(ChannelOption.SO_KEEPALIVE, true)
							.childOption(ChannelOption.TCP_NODELAY, true)
							.childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000);
					return serverBootstrap;
				}));
		return httpServer.handle(httpHandlerAdapter);
	}

}
