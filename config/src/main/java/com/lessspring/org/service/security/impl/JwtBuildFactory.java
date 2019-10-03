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
import com.lessspring.org.utils.GsonUtils;
import com.lessspring.org.utils.PropertiesEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;

import static com.lessspring.org.utils.PropertiesEnum.Jwt.TOKEN_EXPIRE_RANGE;
import static com.lessspring.org.utils.PropertiesEnum.Jwt.TOKEN_STATUS_EXPIRE;
import static com.lessspring.org.utils.PropertiesEnum.Jwt.TOKEN_STATUS_HEALTH;
import static com.lessspring.org.utils.PropertiesEnum.Jwt.TOKEN_STATUS_REFRESH;
import static com.lessspring.org.utils.PropertiesEnum.Jwt.TOKEN_SURVIVAL_MILLISECOND;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.`
 */
@Slf4j
@Component
public class JwtBuildFactory {

    private final Algorithm algorithm;
    private final JwtTokenCache tokenCache;

    public JwtBuildFactory(@Qualifier(value = "JwtTokenAlgorithm") Algorithm algorithm) {
        this.tokenCache = new JwtTokenCache();
        this.algorithm = algorithm;
    }

    private final static String ISS_USER = "LESS_SPRING";

    public String createToken(UserDTO userDTO) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, TOKEN_SURVIVAL_MILLISECOND.getValue());
        String jwt =  JWT
                .create()
                .withIssuer(ISS_USER)
                .withSubject(GsonUtils.toJson(userDTO))
                .withExpiresAt(calendar.getTime())
                .sign(algorithm);
        tokenCache.addToken(jwt, userDTO.getId());
        return jwt;
    }

    public Optional<DecodedJWT> tokenVerify(String jwt) {
        DecodedJWT decodedJWT = null;
        try {
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(ISS_USER).build();
            decodedJWT = verifier.verify(jwt);
        } catch (JWTVerificationException exception) {
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
