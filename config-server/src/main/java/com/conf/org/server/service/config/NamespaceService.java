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
package com.conf.org.server.service.config;

import java.util.List;

import com.conf.org.model.vo.ResponseData;
import com.conf.org.server.pojo.request.NamespaceRequest;
import com.conf.org.server.pojo.vo.NamespaceVO;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public interface NamespaceService {

	/**
	 * find one namespace by name
	 *
	 * @param name namespace-name
	 * @return namespace-name
	 */
	ResponseData<String> findOneNamespaceByName(String name);

	/**
	 * create namespace request
	 *
	 * @param request {@link NamespaceRequest}
	 * @return operation label
	 */
	ResponseData<?> createNamespace(NamespaceRequest request);

	/**
	 * clean namespace request
	 *
	 * @param request {@link NamespaceRequest}
	 * @return operation label
	 */
	ResponseData<?> removeNamespace(NamespaceRequest request);


	/**
	 * query all namespaces
	 *
	 * @return {@link List< NamespaceVO >} all namespace
	 */
	ResponseData<List<NamespaceVO>> queryAll();

	/**
	 * this namespace all owner
	 *
	 * @param namespaceId namespaceId
	 * @return {@link ResponseData<List<String>>}
	 */
	ResponseData<List<String>> allOwnerByNamespace(String namespaceId);

}
