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
package com.lessspring.org.server.repository;

import com.lessspring.org.db.dto.UserDTO;
import com.lessspring.org.model.vo.UserQueryPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Mapper
public interface UserMapper {

	/**
	 * save user info
	 *
	 * @param dto {@link UserDTO}
	 * @return affect row
	 */
	int saveUser(@Param(value = "dto") UserDTO dto);

	/**
	 * modify user info
	 *
	 * @param dto {@link UserDTO}
	 * @return affect row
	 */
	int modifyUser(@Param(value = "dto") UserDTO dto);

	/**
	 * clean user by username
	 * 
	 * @param username username
	 * @return affect row
	 */
	int removeUser(@Param(value = "username") String username);

	/**
	 * find user by username
	 *
	 * @param username username
	 * @return {@link UserDTO}
	 */
	UserDTO findUserByName(@Param(value = "username") String username);

	/**
	 * find all user by page
	 *
	 * @param queryPage {@link UserQueryPage}
	 * @return {@link List<UserDTO>}
	 */
	List<UserDTO> queryAll(@Param(value = "page") UserQueryPage queryPage);

}
