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
package com.lessspring.org.server.service.user.impl;

import com.lessspring.org.Constant;
import com.lessspring.org.EncryptionUtils;
import com.lessspring.org.StrackTracekUtils;
import com.lessspring.org.db.dto.UserDTO;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.model.vo.UserQueryPage;
import com.lessspring.org.raft.TransactionIdManager;
import com.lessspring.org.raft.exception.TransactionException;
import com.lessspring.org.raft.pojo.Datum;
import com.lessspring.org.raft.pojo.Transaction;
import com.lessspring.org.server.exception.NoSuchRoleException;
import com.lessspring.org.server.exception.NotThisResourceException;
import com.lessspring.org.server.exception.ValidationException;
import com.lessspring.org.server.pojo.request.UserRequest;
import com.lessspring.org.server.pojo.vo.ListUserVO;
import com.lessspring.org.server.pojo.vo.UserVO;
import com.lessspring.org.server.repository.NamespacePermissionsMapper;
import com.lessspring.org.server.repository.UserMapper;
import com.lessspring.org.server.service.cluster.ClusterManager;
import com.lessspring.org.server.service.cluster.FailCallback;
import com.lessspring.org.server.service.distributed.BaseTransactionCommitCallback;
import com.lessspring.org.server.service.distributed.TransactionConsumer;
import com.lessspring.org.server.service.user.UserService;
import com.lessspring.org.server.utils.BzConstants;
import com.lessspring.org.server.utils.GsonUtils;
import com.lessspring.org.server.utils.PropertiesEnum;
import com.lessspring.org.server.utils.TransactionUtils;
import com.lessspring.org.server.utils.VOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Service(value = "userService")
public class UserServiceImpl implements UserService {

	private final String createUser = "CREATE_USER";
	private final String modifyUser = "MODIFY_USER";
	private final String deleteUser = "DELETE_USER";

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
		commitCallback.registerConsumer(PropertiesEnum.Bz.USER, createUserConsumer(),
				createUser);
		commitCallback.registerConsumer(PropertiesEnum.Bz.USER, modifyUserConsumer(),
				modifyUser);
		commitCallback.registerConsumer(PropertiesEnum.Bz.USER, removeUserConsumer(),
				deleteUser);
		failCallback = throwable -> null;
	}

	@Override
	public ResponseData<?> createUser(UserRequest request) {
		String key = TransactionUtils.buildTransactionKey(
				PropertiesEnum.InterestKey.USER_DATA, BzConstants.USER_ID,
				request.getUsername());
		request.setId(idManager.query(key).increaseAndObtain());
		Datum datum = new Datum(key, GsonUtils.toJsonBytes(request),
				UserRequest.CLASS_NAME);
		datum.setOperation(createUser);
		return commit(datum);
	}

	@Override
	public ResponseData<?> modifyUser(UserRequest request) {
		String key = TransactionUtils.buildTransactionKey(
				PropertiesEnum.InterestKey.USER_DATA, request.getUsername());
		Datum datum = new Datum(key, GsonUtils.toJsonBytes(request),
				UserRequest.CLASS_NAME);
		datum.setOperation(modifyUser);
		return commit(datum);
	}

	@Override
	public ResponseData<?> removeUser(UserRequest request) {
		String key = TransactionUtils.buildTransactionKey(
				PropertiesEnum.InterestKey.USER_DATA, request.getUsername());
		Datum datum = new Datum(key, GsonUtils.toJsonBytes(request),
				UserRequest.CLASS_NAME);
		datum.setOperation(deleteUser);
		return commit(datum);
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

	private TransactionConsumer<Transaction> createUserConsumer() {
		return new TransactionConsumer<Transaction>() {
			@Override
			public void accept(Transaction transaction) throws Throwable {
				UserRequest request = GsonUtils.toObj(transaction.getData(),
						UserRequest.class);
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
			}

			@Override
			public void onError(TransactionException te) {
				log.error("queryAll user have some error : {}",
						StrackTracekUtils.stackTrace(te));
			}
		};
	}

	private TransactionConsumer<Transaction> modifyUserConsumer() {
		return new TransactionConsumer<Transaction>() {
			@Override
			public void accept(Transaction transaction) throws Throwable {
				UserRequest request = GsonUtils.toObj(transaction.getData(),
						UserRequest.class);
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
							.password(EncryptionUtils
									.encryptByBcrypt(request.getPassword()))
							.roleType(request.getRole().getType()).build();
					userMapper.modifyUser(dto);
				}
				throw new ValidationException();
			}

			@Override
			public void onError(TransactionException te) {
				log.error("queryAll user have some error : {}",
						StrackTracekUtils.stackTrace(te));
			}
		};
	}

	private TransactionConsumer<Transaction> removeUserConsumer() {
		return new TransactionConsumer<Transaction>() {
			@Override
			public void accept(Transaction transaction) throws Throwable {
				UserRequest request = GsonUtils.toObj(transaction.getData(),
						UserRequest.class);
				userMapper.removeUser(request.getUsername());
			}

			@Override
			public void onError(TransactionException te) {
				log.error("queryAll user have some error : {}",
						StrackTracekUtils.stackTrace(te));
			}
		};
	}

	private ResponseData<?> commit(Datum datum) {
		datum.setBz(PropertiesEnum.Bz.USER.name());
		CompletableFuture<ResponseData<Boolean>> future = clusterManager.commit(datum,
				failCallback);
		try {
			return future.get(10_000L, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException | ExecutionException | TimeoutException e) {
			return ResponseData.fail(e);
		}
	}
}
