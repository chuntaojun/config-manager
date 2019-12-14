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
package com.lessspring.org.server.service.security.impl;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Resource;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.lessspring.org.db.dto.UserDTO;
import com.lessspring.org.server.exception.UserNotFoundException;
import com.lessspring.org.server.exception.VerifyException;
import com.lessspring.org.model.vo.JwtResponse;
import com.lessspring.org.model.vo.LoginRequest;
import com.lessspring.org.server.repository.UserMapper;
import com.lessspring.org.server.service.common.CacheOperation;
import com.lessspring.org.server.service.security.SecurityService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import static com.lessspring.org.server.utils.PropertiesEnum.Jwt.TOKEN_STATUS_EXPIRE;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Service
public class SecurityServiceImpl implements SecurityService {

	private final CacheOperation cacheOperation;
	private final JwtBuildFactory jwtBuildFactory;
	@Resource
	private UserMapper userMapper;

	public SecurityServiceImpl(CacheOperation cacheOperation,
			JwtBuildFactory jwtBuildFactory) {
		this.cacheOperation = cacheOperation;
		this.jwtBuildFactory = jwtBuildFactory;
	}

	@Override
	public JwtResponse apply4Authorization(LoginRequest loginRequest) {
		final JwtResponse[] jwt = new JwtResponse[] { null };
		final String userName = loginRequest.getUsername();
		Optional<UserDTO> userDTO = cacheOperation.getObj(userName);
		if (!userDTO.isPresent()) {
			UserDTO dto = userMapper.findUserByName(loginRequest.getUsername());
			if (dto == null) {
				throw new UserNotFoundException();
			}
			cacheOperation.put(userName, dto, Duration.ofMinutes(15).toMillis());
			userDTO = Optional.of(dto);
		}
		userDTO.ifPresent(dto -> {
			if (!Objects.equals(loginRequest.getPassword(), dto.getPassword())) {
				throw new VerifyException("Username or password is wrong");
			}
			jwt[0] = jwtBuildFactory.createToken(dto);
		});
		return jwt[0];
	}

	@Override
	public Optional<DecodedJWT> verify(String token) {
		return jwtBuildFactory.tokenVerify(token);
	}

	@Override
	public boolean isExpire(String token) {
		Optional<DecodedJWT> optional = verify(token);
		return optional
				.map(decodedJWT -> TOKEN_STATUS_EXPIRE
						.compareTo(jwtBuildFactory.isExpire(decodedJWT)) == 0)
				.orElse(true);
	}

}
