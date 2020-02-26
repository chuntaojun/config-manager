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
package com.conf.org.server.service.user.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.conf.org.EncryptionUtils;
import com.conf.org.db.dto.UserDTO;
import com.conf.org.model.vo.ResponseData;
import com.conf.org.model.vo.UserQueryPage;
import com.conf.org.raft.TransactionIdManager;
import com.conf.org.server.exception.NoSuchRoleException;
import com.conf.org.server.exception.NotThisResourceException;
import com.conf.org.server.repository.NamespacePermissionsMapper;
import com.conf.org.server.repository.UserMapper;
import com.conf.org.server.service.cluster.ClusterManager;
import com.conf.org.server.service.cluster.FailCallback;
import com.conf.org.server.service.distributed.BaseTransactionCommitCallback;
import com.conf.org.server.exception.ValidationException;
import com.conf.org.server.pojo.request.UserRequest;
import com.conf.org.server.pojo.vo.ListUserVO;
import com.conf.org.server.pojo.vo.UserVO;
import com.conf.org.server.service.user.UserService;
import com.conf.org.server.utils.PropertiesEnum;
import com.conf.org.server.utils.VOUtils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Service(value = "userService")
public class UserServiceImpl implements UserService {

	private final BaseTransactionCommitCallback commitCallback;
	private final ClusterManager clusterManager;
	private FailCallback failCallback;

	@Resource
	private UserMapper userMapper;

	@Resource
	private NamespacePermissionsMapper permissionsMapper;

	@Autowired
	private TransactionIdManager idManager;

	public UserServiceImpl(BaseTransactionCommitCallback commitCallback,
			ClusterManager clusterManager) {
		this.commitCallback = commitCallback;
		this.clusterManager = clusterManager;
	}

	@PostConstruct
	public void init() {
		failCallback = throwable -> null;
	}

	@Override
	public ResponseData<?> createUser(UserRequest request) {
		try {
			PropertiesEnum.Role.choose(request.getRole().getType());
		}
		catch (Exception e) {
			throw new NoSuchRoleException();
		}
		UserDTO dto = UserDTO.builder().id(request.getId())
				.username(request.getUsername())
				.password(EncryptionUtils.encryptByBcrypt(request.getPassword()))
				.roleType(request.getRole().getType()).build();
		userMapper.saveUser(dto);
		return ResponseData.success();
	}

	@Override
	public ResponseData<?> modifyUser(UserRequest request) {
		UserDTO dtoDB = userMapper.findUserByName(request.getUsername());
		if (dtoDB == null) {
			throw new NotThisResourceException("Not this user info");
		}
		try {
			PropertiesEnum.Role.choose(request.getRole().getType());
		}
		catch (Exception e) {
			throw new NoSuchRoleException();
		}
		if (EncryptionUtils.matchesByBcrypt(request.getOldPassword(),
				dtoDB.getPassword())) {
			UserDTO dto = UserDTO.builder().username(request.getUsername())
					.password(EncryptionUtils.encryptByBcrypt(request.getPassword()))
					.roleType(request.getRole().getType()).build();
			userMapper.modifyUser(dto);
		}
		throw new ValidationException();
	}

	@Override
	public ResponseData<?> removeUser(UserRequest request) {
		userMapper.removeUser(request.getUsername());
		return ResponseData.success();
	}

	@Override
	public ResponseData<ListUserVO> queryAll(UserQueryPage queryPage) {
		final ListUserVO result = new ListUserVO();
		List<UserDTO> dtos = userMapper.queryAll(queryPage);
		dtos = CollectionUtils.isEmpty(dtos) ? Collections.emptyList() : dtos;
		List<UserVO> vos = new ArrayList<>();
		for (UserDTO dto : dtos) {
			UserVO vo = VOUtils.convertUserVo(dto);
			vo.setResources(permissionsMapper.findNamespaceIdByUserId(dto.getId()));
			vos.add(vo);
		}
		result.setTotal(userMapper.count(queryPage.getUsername()).longValue());
		result.setUserVOS(vos);
		return ResponseData.success(result);
	}

}
