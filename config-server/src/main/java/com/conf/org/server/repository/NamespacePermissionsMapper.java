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
package com.conf.org.server.repository;

import java.util.List;

import com.conf.org.db.dto.AuthorityDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Mapper
public interface NamespacePermissionsMapper {

	/**
	 * find namespaceId by userId
	 *
	 * @param userId user-id
	 * @return namespace-id
	 */
	List<String> findNamespaceIdByUserId(@Param(value = "userId") Long userId);

	/**
	 * find this namespaceId all owners
	 *
	 * @param namespaceId namespaceId
	 * @return owners
	 */
	List<String> findUsersByNamespaceId(@Param(value = "namespaceId") String namespaceId);

	/**
	 * remove namesapce access permission by user-id and namespace-id
	 * 
	 * @param namespaceId namespaceId
	 * @param userId userId
	 * @return affect row
	 */
	int removePermissionByUserIdAndNamespaceId(
			@Param(value = "namesapceId") String namespaceId,
			@Param(value = "userId") Long userId);

	/**
	 * save access namespace resource by {@link AuthorityDTO}
	 * 
	 * @param dto {@link AuthorityDTO}
	 * @return affect row
	 */
	int savePermission(@Param(value = "dto") AuthorityDTO dto);

}
