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
package com.lessspring.org.service.config.impl;

import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.model.vo.BaseConfigRequest;
import com.lessspring.org.model.vo.DeleteConfigRequest;
import com.lessspring.org.model.vo.PublishConfigRequest;
import com.lessspring.org.pojo.request.DeleteConfigHistory;
import com.lessspring.org.pojo.request.PublishConfigHistory;
import com.lessspring.org.service.config.PersistentHandler;
import com.lessspring.org.service.encryption.EncryptionService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Primary
@Service(value = "encryptionPersistentHandler")
public class EncryptionPersistentHandler implements PersistentHandler {

	@Autowired
	private EncryptionService encryptionService;

	@Autowired
	@Qualifier(value = "cachePersistentHandler")
	private PersistentHandler persistentHandler;

	public EncryptionPersistentHandler() {
	}

	@Override
	public ConfigInfo readConfigContent(String namespaceId, BaseConfigRequest request) {
		return persistentHandler.readConfigContent(namespaceId, request);
	}

	@Override
	public boolean saveConfigInfo(String namespaceId, PublishConfigRequest request) {
		encryptionService.handle(request);
		return persistentHandler.saveConfigInfo(namespaceId, request);
	}

	@Override
	public boolean modifyConfigInfo(String namespaceId, PublishConfigRequest request) {
		encryptionService.handle(request);
		return persistentHandler.modifyConfigInfo(namespaceId, request);
	}

	@Override
	public boolean removeConfigInfo(String namespaceId, DeleteConfigRequest request) {
		return persistentHandler.removeConfigInfo(namespaceId, request);
	}

	@Override
	public boolean saveConfigHistory(String namespaceId,
			PublishConfigHistory publishConfigHistory) {
		return persistentHandler.saveConfigHistory(namespaceId, publishConfigHistory);
	}

	@Override
	public boolean removeConfigHistory(String namespaceId,
			DeleteConfigHistory deleteConfigHistory) {
		return persistentHandler.removeConfigHistory(namespaceId, deleteConfigHistory);
	}

	@Override
	public int priority() {
		return HIGH_PRIORITY;
	}
}
