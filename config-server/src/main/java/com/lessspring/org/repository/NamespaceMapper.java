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
package com.lessspring.org.repository;

import java.util.List;

import com.lessspring.org.db.dto.NamespaceDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Mapper
public interface NamespaceMapper {

	/**
	 * find namespace by name
	 *
	 * @param name namespace-name
	 * @return namespace-name
	 */
	String findNamespaceByName(@Param(value = "name") String name);

	/**
	 * find {@link NamespaceDTO} by namespace-id
	 *
	 * @param name namespace-name
	 * @return {@link NamespaceDTO}
	 */
	NamespaceDTO findNamespaceDTOByName(@Param(value = "name") String name);

	/**
	 * save namespace
	 *
	 * @param dto {@link NamespaceDTO}
	 * @return affect row
	 */
	int saveNamespace(@Param(value = "dto") NamespaceDTO dto);

	/**
	 * clean namespace by namespace-name
	 *
	 * @param namespace namespace-name
	 * @return affect row
	 */
	int removeNamespace(@Param(value = "namespace") String namespace);

	/**
	 * save access namespace token
	 *
	 * @param name namespace name
	 * @param token auth token
	 * @return affect row
	 */
	int saveNamespaceAuthToken(@Param(value = "name") String name,
			@Param(value = "token") String token);

	/**
	 * query all namespaces
	 *
	 * @return {@link List<String>}
	 */
	List<NamespaceDTO> queryAll();

}
