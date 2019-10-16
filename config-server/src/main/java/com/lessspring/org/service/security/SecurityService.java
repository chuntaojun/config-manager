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
package com.lessspring.org.service.security;

import java.util.Optional;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.lessspring.org.model.vo.JwtResponse;
import com.lessspring.org.model.vo.LoginRequest;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public interface SecurityService {

	/**
	 * Apply for authorization code
	 *
	 * @param loginRequest {@link LoginRequest}
	 * @return jwt token
	 */
	JwtResponse apply4Authorization(LoginRequest loginRequest);

	/**
	 * verify token
	 *
	 * @param token jwt token
	 * @return Whether for this token issued by the component
	 */
	Optional<DecodedJWT> verify(String token);

	/**
	 * this token has been expire
	 *
	 * @param token jwt token
	 * @return is expire label
	 */
	boolean isExpire(String token);

}
