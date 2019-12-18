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
package com.lessspring.org.server.handler.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import com.google.gson.reflect.TypeToken;
import com.lessspring.org.jvm.JvmUtils;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.observer.Occurrence;
import com.lessspring.org.observer.Publisher;
import com.lessspring.org.raft.TransactionIdManager;
import com.lessspring.org.server.configuration.security.NeedAuth;
import com.lessspring.org.server.configuration.tps.LimitRule;
import com.lessspring.org.server.configuration.tps.OpenTpsLimit;
import com.lessspring.org.server.configuration.tps.TpsConfiguration;
import com.lessspring.org.server.configuration.tps.TpsSetting;
import com.lessspring.org.server.handler.SystemHandler;
import com.lessspring.org.server.pojo.request.PublishQpsRequest;
import com.lessspring.org.server.service.dump.DumpService;
import com.lessspring.org.server.service.publish.TraceAnalyzer;
import com.lessspring.org.server.utils.GsonUtils;
import com.lessspring.org.server.utils.PropertiesEnum;
import com.lessspring.org.server.utils.RenderUtils;
import com.lessspring.org.server.utils.SchedulerUtils;
import com.lessspring.org.server.utils.SystemEnv;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationBeanFactoryMetadata;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Service
@OpenTpsLimit
public class SystemHandlerImpl extends Publisher<TpsSetting> implements SystemHandler {

	private final LoggingSystem loggingSystem;
	private final DumpService dumpService;
	private final TraceAnalyzer traceAnalyzer;
	private final TpsConfiguration.TpsAnnotationProcessor tpsAnnotationProcessor;
	private SystemEnv systemEnv;
	private StandardEnvironment environment = new StandardEnvironment();
	private ConfigurationBeanFactoryMetadata beanFactoryMetadata;

	@Autowired
	private TransactionIdManager idManager;

	@Autowired
	private TpsSetting tpsSetting;

	public SystemHandlerImpl(LoggingSystem loggingSystem, DumpService dumpService,
			TraceAnalyzer traceAnalyzer,
			TpsConfiguration.TpsAnnotationProcessor tpsAnnotationProcessor,
			ApplicationContext applicationContext) {
		this.loggingSystem = loggingSystem;
		this.dumpService = dumpService;
		this.traceAnalyzer = traceAnalyzer;
		this.tpsAnnotationProcessor = tpsAnnotationProcessor;
		this.beanFactoryMetadata = applicationContext.getBean(
				ConfigurationBeanFactoryMetadata.BEAN_NAME,
				ConfigurationBeanFactoryMetadata.class);
	}

	@PostConstruct
	public void init() {
		systemEnv = SystemEnv.getSingleton();
		registerWatcher(tpsAnnotationProcessor);
	}

	@Override
	@NeedAuth(role = PropertiesEnum.Role.ADMIN)
	public Mono<ServerResponse> changeLogLevel(ServerRequest request) {
		final String logLevel = request.queryParam("logLevel").orElse("info");
		LogLevel newLevel;
		try {
			newLevel = LogLevel.valueOf(logLevel);
		}
		catch (Exception e) {
			newLevel = LogLevel.INFO;
		}
		LogLevel finalNewLevel = newLevel;
		loggingSystem.getLoggerConfigurations().forEach(
				conf -> loggingSystem.setLogLevel(conf.getName(), finalNewLevel));
		return RenderUtils.render(Mono.just(ResponseData.success())).subscribeOn(
				Schedulers.fromExecutor(SchedulerUtils.getSingleton().WEB_HANDLER));
	}

	@Override
	@LimitRule(resource = "system-resource", qps = 1, timeUnit = TimeUnit.MINUTES)
	@NeedAuth(role = PropertiesEnum.Role.ADMIN)
	public Mono<ServerResponse> forceDumpConfig(ServerRequest request) {
		dumpService.forceDump(false);
		return RenderUtils.render(ResponseData.success()).subscribeOn(
				Schedulers.fromExecutor(SchedulerUtils.getSingleton().WEB_HANDLER));
	}

	@Override
	public Mono<ServerResponse> publishLog(ServerRequest request) {
		return RenderUtils.render(ResponseData.success(traceAnalyzer.analyzePublishLog()))
				.subscribeOn(Schedulers
						.fromExecutor(SchedulerUtils.getSingleton().WEB_HANDLER));
	}

	@Override
	@LimitRule(resource = "system-resource", qps = 1, timeUnit = TimeUnit.MINUTES)
	@NeedAuth(role = PropertiesEnum.Role.ADMIN)
	public Mono<ServerResponse> jvmHeapDump(ServerRequest request) {
		final String fileName = systemEnv.jvmHeapDumpFileNameSuppiler.get();
		log.info("[Jvm heap dump] file name : {}", fileName);
		final File[] files = new File[] { null };
		Supplier<Resource> callable = () -> {
			try {
				final boolean isLive = Boolean.parseBoolean(
						(String) request.attribute("isLive").orElse("true"));
				final File file = JvmUtils.jMap(fileName, isLive);
				files[0] = file;
				return new UrlResource(file.toURI());
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
		return RenderUtils.render(callable.get()).doOnTerminate(() -> {
			if (Objects.nonNull(files[0])) {
				if (files[0].exists()) {
					log.warn(
							"[JvmDump Handler] auto delete file when this request finish");
					files[0].delete();
				}
			}
		}).subscribeOn(
				Schedulers.fromExecutor(SchedulerUtils.getSingleton().WEB_HANDLER));
	}

	@SuppressWarnings("all")
	@Override
	@NeedAuth(role = PropertiesEnum.Role.ADMIN)
	public Mono<ServerResponse> publishQpsSetting(ServerRequest request) {
		return request.bodyToMono(String.class).map(s -> (ResponseData<String>) GsonUtils
				.toObj(s, new TypeToken<ResponseData<String>>() {
				})).map(responseData -> {
					String result = responseData.getData();
					Properties properties = new Properties();
					try {
						properties.load(new StringReader(result));
					}
					catch (IOException e) {
						throw new RuntimeException(e);
					}
					return properties;
				}).map(properties -> {
					String name = "qps";
					PropertySource propertySource = new PropertiesPropertySource(name,
							properties);
					if (environment.containsProperty(name)) {
						environment.getPropertySources().replace(name, propertySource);
					}
					else {
						environment.getPropertySources().addLast(propertySource);
					}
					return Binder.get(environment);
				}).flatMap(binder -> {
					ResolvableType type = getBeanType(tpsSetting);
					Bindable target = Bindable.of(type).withExistingValue(tpsSetting);
					binder.bind(PublishQpsRequest.PREFIX, target);
					tpsAnnotationProcessor.onNotify(Occurrence.newInstance(tpsSetting),
							this);
					return RenderUtils.render(Mono.just(ResponseData.success()));
				})
				.onErrorResume(new Function<Throwable, Mono<? extends ServerResponse>>() {
					@Override
					public Mono<? extends ServerResponse> apply(Throwable throwable) {
						return RenderUtils
								.render(Mono.just(ResponseData.fail(throwable)));
					}
				}).subscribeOn(Schedulers
						.fromExecutor(SchedulerUtils.getSingleton().WEB_HANDLER));
	}

	@Override
	public Mono<ServerResponse> queryQpsSetting(ServerRequest request) {
		return RenderUtils.render(Mono.just(tpsSetting)).subscribeOn(
				Schedulers.fromExecutor(SchedulerUtils.getSingleton().WEB_HANDLER));
	}

	@Override
	public Mono<ServerResponse> getAllTransactionIdInfo(ServerRequest request) {
		return RenderUtils.render(Mono.just(idManager.all())).subscribeOn(
				Schedulers.fromExecutor(SchedulerUtils.getSingleton().WEB_HANDLER));
	}

	private ResolvableType getBeanType(Object bean) {
		Method factoryMethod = this.beanFactoryMetadata.findFactoryMethod("tpsSetting");
		if (factoryMethod != null) {
			return ResolvableType.forMethodReturnType(factoryMethod);
		}
		return ResolvableType.forClass(bean.getClass());
	}
}
