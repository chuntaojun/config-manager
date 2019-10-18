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
package com.lessspring.org.service.config;

import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.model.vo.BaseConfigRequest;
import com.lessspring.org.model.vo.DeleteConfigRequest;
import com.lessspring.org.model.vo.PublishConfigRequest;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public interface PersistentHandler {

	/**
	 * read config-info
	 *
	 * @param namespaceId namespaceId
	 * @param request {@link BaseConfigRequest}
	 * @return {@link ConfigInfo}
	 */
	ConfigInfo readConfigContent(String namespaceId, BaseConfigRequest request);

	/**
	 * save config-info
	 *
	 * @param namespaceId namespaceId
	 * @param request {@link PublishConfigRequest}
	 * @return save result
	 */
	boolean saveConfigInfo(String namespaceId, PublishConfigRequest request);

	/**
	 * modify config-info
	 *
	 * @param namespaceId namespaceId
	 * @param request {@link PublishConfigRequest}
	 * @return modify result
	 */
	boolean modifyConfigInfo(String namespaceId, PublishConfigRequest request);

	/**
	 * remove config-info
	 *
	 * @param namespaceId namespaceId
	 * @param request {@link DeleteConfigRequest}
	 * @return delete result
	 */
	boolean removeConfigInfo(String namespaceId, DeleteConfigRequest request);

}