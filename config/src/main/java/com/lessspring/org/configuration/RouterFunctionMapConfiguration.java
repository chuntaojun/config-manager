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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.handler.AbstractHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Configuration
public class RouterFunctionMapConfiguration extends AbstractHandlerMapping implements InitializingBean {

    @Nullable
    private RouterFunction<?> routerFunction;

    private List<HttpMessageReader<?>> messageReaders = Collections.emptyList();

    @Override
    public void afterPropertiesSet() throws Exception {
        if (CollectionUtils.isEmpty(this.messageReaders)) {
            ServerCodecConfigurer codecConfigurer = ServerCodecConfigurer.create();
            this.messageReaders = codecConfigurer.getReaders();
        }
        if (this.routerFunction == null) {
            initRouterFunctions();
        }
    }

    private void initRouterFunctions() {
        List<RouterFunction<?>> routerFunctions = routerFunctions();
        this.routerFunction = routerFunctions
                .stream()
                .reduce((routerFunction1, other) -> routerFunction1.andOther(other))
                .orElse(null);
    }

    private List<RouterFunction<?>> routerFunctions() {
        SortedRouterFunctionsContainer container = new SortedRouterFunctionsContainer();
        obtainApplicationContext().getAutowireCapableBeanFactory().autowireBean(container);
        return CollectionUtils.isEmpty(container.routerFunctions) ? Collections.emptyList() : container.routerFunctions;
    }

    @Override
    protected Mono<?> getHandlerInternal(ServerWebExchange exchange) {
        return null;
    }

    private static class SortedRouterFunctionsContainer {

        @Nullable
        private List<RouterFunction<?>> routerFunctions;

        @Autowired(required = false)
        public void setRouterFunctions(List<RouterFunction<?>> routerFunctions) {
            this.routerFunctions = routerFunctions;
        }
    }

}
