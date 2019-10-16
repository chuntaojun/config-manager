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
package com.lessspring.org.service.user.impl;

import javax.annotation.Resource;

import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.repository.UserMapper;
import com.lessspring.org.service.user.UserService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Service(value = "userService")
public class UserServiceImpl implements UserService {

	@Resource
	private UserMapper userMapper;

	@Override
	public ResponseData<Boolean> createUser() {
		return null;
	}

	@Override
	public ResponseData<Boolean> modifyUser() {
		return null;
	}

	@Override
	public ResponseData<Boolean> removeUser() {
		return null;
	}
}
