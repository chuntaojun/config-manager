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

import io.netty.channel.EventLoopGroup;
import io.netty.channel.kqueue.KQueueEventLoopGroup;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.boot.web.server.Http2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Custom netty container related parameters
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Configuration
public class ConfigNettyReactiveWebServerFactory {

	@Value("${com.lessspring.org.config-manager.netty.loopThreads}")
	private int loopThreads;

	@Value("${com.lessspring.org.config-manager.netty.workerThreads}")
	private int workerThreads;

	@Bean
	public NettyReactiveWebServerFactory nettyReactiveWebServerFactory() {
		NettyReactiveWebServerFactory factory = new NettyReactiveWebServerFactory() {
			@Override
			public void setHttp2(Http2 http2) {
				http2.setEnabled(true);
			}

		};
		factory.addServerCustomizers((NettyServerCustomizer) httpServer -> httpServer
				.tcpConfiguration(tcpServer -> tcpServer.bootstrap(serverBootstrap -> {
					EventLoopGroup core = new KQueueEventLoopGroup(loopThreads);
					EventLoopGroup worker = new KQueueEventLoopGroup(workerThreads);
					serverBootstrap.group(core, worker);
					return serverBootstrap;
				})));
		return factory;
	}
}
