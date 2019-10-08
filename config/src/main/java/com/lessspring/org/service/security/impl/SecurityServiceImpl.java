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
package com.lessspring.org.service.security.impl;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.lessspring.org.db.dto.UserDTO;
import com.lessspring.org.model.vo.LoginRequest;
import com.lessspring.org.repository.UserMapper;
import com.lessspring.org.service.security.SecurityService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.lessspring.org.utils.PropertiesEnum.Jwt.TOKEN_STATUS_EXPIRE;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Service
public class SecurityServiceImpl implements SecurityService {

    @Resource
    private UserMapper userMapper;

    private final JwtBuildFactory jwtBuildFactory;

    public SecurityServiceImpl(JwtBuildFactory jwtBuildFactory) {
        this.jwtBuildFactory = jwtBuildFactory;
    }

    @Override
    public String apply4Authorization(LoginRequest loginRequest) {
        Optional<UserDTO> userDTO = Optional.ofNullable(userMapper.findUserByName(loginRequest.getUsername()));
        String[] jwt = new String[]{null};
        userDTO.ifPresent(dto -> jwt[0] = jwtBuildFactory.createToken(dto));
        return jwt[0];
    }

    @Override
    public String refreshAuth(String oldToken, LoginRequest loginRequest) {
        return null;
    }

    @Override
    public Optional<DecodedJWT> verify(String token) {
        return jwtBuildFactory.tokenVerify(token);
    }

    @Override
    public boolean isExpire(String token) {
        Optional<DecodedJWT> optional = verify(token);
        return optional.map(decodedJWT -> TOKEN_STATUS_EXPIRE.compareTo(jwtBuildFactory.isExpire(decodedJWT)) == 0)
                .orElse(true);
    }

}
