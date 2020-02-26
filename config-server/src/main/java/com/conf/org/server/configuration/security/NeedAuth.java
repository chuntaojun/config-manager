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
package com.conf.org.server.configuration.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.conf.org.server.service.security.AuthorityProcessor;
import com.conf.org.server.service.security.impl.NameAuthorityProcessorImpl;
import com.conf.org.server.utils.PropertiesEnum;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NeedAuth {

	/**
	 * Return to check parameter name
	 *
	 * @return parameter name
	 */
	String argueName() default "";

	/**
	 * Return handler to execute privilege check
	 *
	 * @return Class<? extends AuthorityProcessor>
	 */
	Class<? extends AuthorityProcessor> handler() default NameAuthorityProcessorImpl.class;

	/**
	 * this resource can access user role
	 *
	 * @return default type is {@link PropertiesEnum.Role#CUSTOMER}
	 */
	PropertiesEnum.Role[] role() default PropertiesEnum.Role.CUSTOMER;

	/**
	 * Operations that can be involved
	 *
	 * @return default value is {@link PropertiesEnum.Operation#WR}
	 */
	PropertiesEnum.Operation operation() default PropertiesEnum.Operation.WR;

}