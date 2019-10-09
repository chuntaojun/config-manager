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

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.lessspring.org.db.dto.UserDTO;
import com.lessspring.org.model.vo.JwtResponse;
import com.lessspring.org.pojo.Privilege;
import com.lessspring.org.raft.NodeManager;
import com.lessspring.org.raft.vo.ServerNode;
import com.lessspring.org.repository.NamespacePermissionsMapper;
import com.lessspring.org.utils.GsonUtils;
import com.lessspring.org.utils.PropertiesEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;

import static com.lessspring.org.utils.PropertiesEnum.Jwt.TOKEN_EXPIRE_RANGE;
import static com.lessspring.org.utils.PropertiesEnum.Jwt.TOKEN_STATUS_EXPIRE;
import static com.lessspring.org.utils.PropertiesEnum.Jwt.TOKEN_STATUS_HEALTH;
import static com.lessspring.org.utils.PropertiesEnum.Jwt.TOKEN_STATUS_REFRESH;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.`
 */
@Slf4j
@Component
public class JwtBuildFactory {

    @Value("${com.lesspring.org.config-manger.jwt.survival.time.second}")
    private int tokenSurvival;

    @Resource
    private NamespacePermissionsMapper permissionsMapper;

    private final NodeManager nodeManager = NodeManager.getInstance();

    private final Algorithm algorithm;

    public JwtBuildFactory(@Qualifier(value = "JwtTokenAlgorithm") Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public JwtResponse createToken(UserDTO userDTO) {
        final JwtResponse response = new JwtResponse();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, tokenSurvival);
        Privilege privilege = new Privilege();
        privilege.setRole(PropertiesEnum.Role.choose(userDTO.getRoleType() == null ?
                PropertiesEnum.Role.CUSTOMER.getType() : userDTO.getRoleType()));
        privilege.setUsername(userDTO.getUsername());
        privilege.setOwnerNamespace(permissionsMapper.findNamespaceIdByUserId(userDTO.getId()));
        String jwt = JWT
                .create()
                .withIssuer(GsonUtils.toJson(nodeManager.getSelf()))
                .withSubject(GsonUtils.toJson(privilege))
                .withExpiresAt(calendar.getTime())
                .sign(algorithm);
        response.setExpireTime(calendar.getTime().getTime());
        response.setToken(jwt);
        return response;
    }

    public Optional<DecodedJWT> tokenVerify(String jwt) {
        DecodedJWT decodedJWT = null;
        boolean goOn = false;
        for (ServerNode node : nodeManager.serverNodes()) {
            try {
                JWTVerifier verifier = JWT.require(algorithm)
                        .withIssuer(GsonUtils.toJson(node)).build();
                decodedJWT = verifier.verify(jwt);
                goOn = false;
            } catch (JWTVerificationException exception) {
                log.error(exception.getMessage());
                goOn = true;
            }
            if (!goOn) {
                break;
            }
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
