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
package com.lessspring.org.service.common.impl;

import javax.annotation.PostConstruct;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.lessspring.org.pojo.event.email.BaseEmailEvent;
import com.lessspring.org.pojo.event.email.ErrorEmailEvent;
import com.lessspring.org.pojo.event.email.WarnEmailEvent;
import com.lessspring.org.service.common.EmailService;

import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Component
public class EmailServiceImpl implements EmailService {

	private EventBus eventBus;

	@PostConstruct
	public void init() {
		eventBus = new EventBus("com.lessspring.org.config-manager.emailBus");
		eventBus.register(this);
	}

	@Override
	public void publishEmailEvent(BaseEmailEvent baseEmailEvent) {
		eventBus.post(baseEmailEvent);
	}

	@Subscribe
	public void onWarnEvent(WarnEmailEvent event) throws Exception {

	}

	@Subscribe
	public void onErrorEvent(ErrorEmailEvent event) throws Exception {

	}
}
