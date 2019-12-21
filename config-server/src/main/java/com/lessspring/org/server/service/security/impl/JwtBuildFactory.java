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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;

import javax.annotation.Resource;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.lessspring.org.db.dto.UserDTO;
import com.lessspring.org.model.vo.JwtResponse;
import com.lessspring.org.server.pojo.Privilege;
import com.lessspring.org.server.repository.NamespacePermissionsMapper;
import com.lessspring.org.utils.GsonUtils;
import com.lessspring.org.server.utils.PropertiesEnum;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import static com.lessspring.org.server.utils.PropertiesEnum.Jwt.TOKEN_EXPIRE_RANGE;
import static com.lessspring.org.server.utils.PropertiesEnum.Jwt.TOKEN_STATUS_EXPIRE;
import static com.lessspring.org.server.utils.PropertiesEnum.Jwt.TOKEN_STATUS_HEALTH;
import static com.lessspring.org.server.utils.PropertiesEnum.Jwt.TOKEN_STATUS_REFRESH;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.`
 */
@Slf4j
@Component
public class JwtBuildFactory {

	private final Algorithm algorithm;
	@Value("${com.lessspring.org.config-manger.jwt.survival.time.second}")
	private int tokenSurvival;
	@Value("${com.lessspring.org.config-manger.jwt.signature}")
	private String signature;
	@Resource
	private NamespacePermissionsMapper permissionsMapper;

	public JwtBuildFactory(@Qualifier(value = "JwtTokenAlgorithm") Algorithm algorithm) {
		this.algorithm = algorithm;
	}

	public Tuple2<Privilege, JwtResponse> createToken(UserDTO userDTO) {
		final JwtResponse response = new JwtResponse();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.SECOND, tokenSurvival);
		Privilege privilege = new Privilege();
		privilege.setUserId(userDTO.getId());
		privilege.setRole(PropertiesEnum.Role.choose(
				userDTO.getRoleType() == null ? PropertiesEnum.Role.CUSTOMER.getType()
						: userDTO.getRoleType()));
		privilege.setUsername(userDTO.getUsername());
		privilege.setOwnerNamespaces(
				permissionsMapper.findNamespaceIdByUserId(userDTO.getId()));
		String jwt = JWT.create().withIssuer(signature)
				.withSubject(GsonUtils.toJson(privilege))
				.withExpiresAt(calendar.getTime()).sign(algorithm);
		response.setExpireTime(calendar.getTime().getTime());
		response.setToken(jwt);
		return Tuples.of(privilege, response);
	}

	public Optional<DecodedJWT> tokenVerify(String jwt) {
		DecodedJWT decodedJWT = null;
		try {
			JWTVerifier verifier = JWT.require(algorithm).withIssuer(signature).build();
			decodedJWT = verifier.verify(jwt);
		}
		catch (JWTVerificationException exception) {
			log.error(exception.getMessage());
		}
		return Optional.ofNullable(decodedJWT);
	}

	public PropertiesEnum.Jwt isExpire(DecodedJWT decodedJWT) {
		long timeMisc = decodedJWT.getExpiresAt().getTime() - System.currentTimeMillis();
		if (timeMisc < 0) {
			return TOKEN_STATUS_EXPIRE;
		}
		if (timeMisc <= TOKEN_EXPIRE_RANGE.getValue()) {
			return TOKEN_STATUS_REFRESH;
		}
		return TOKEN_STATUS_HEALTH;
	}

}
