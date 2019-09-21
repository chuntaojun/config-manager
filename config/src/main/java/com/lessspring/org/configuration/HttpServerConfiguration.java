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

import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import reactor.netty.http.server.HttpServer;

import java.util.Collections;
import java.util.Objects;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Configuration
public class HttpServerConfiguration {

    private final Environment environment;

    public HttpServerConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public HttpServer httpServerForService(@Qualifier(value = "configRouterImpl") RouterFunction<?> routerFunction) {
        return getHttpServer(routerFunction);
    }

    @Bean
    public HttpMessageConverters httpMessageConverters() {
        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        gsonHttpMessageConverter.setGson(new GsonBuilder().create());
        return new HttpMessageConverters(true, Collections.singletonList(gsonHttpMessageConverter));
    }

    /**
     * <pre>
     * 抽取的函数, 实现具体的 HttpServer 部署实现工作
     * </pre>
     * @param routerFunction 路由实例
     * @return {@link HttpServer}
     */
    private HttpServer getHttpServer(RouterFunction<?> routerFunction) {
        HttpHandler handler = RouterFunctions.toHttpHandler(routerFunction);
        ReactorHttpHandlerAdapter httpHandlerAdapter = new ReactorHttpHandlerAdapter(handler);
        HttpServer httpServer = HttpServer.create()
                .host("localhost")
                .port(Integer.parseInt(Objects.requireNonNull(environment.getProperty("server.port"))));
        httpServer.handle(httpHandlerAdapter);
        return httpServer;
    }

}
