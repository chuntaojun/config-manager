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
package com.lessspring.org.pojo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lessspring.org.utils.PropertiesEnum;

/**
 * The custom of a simple access objects
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class Privilege {

	private Long userId;
	private String username;
	private List<String> ownerNamespaces;
	private transient String jwt;
	private PropertiesEnum.Role role;
	private Map<String, Object> attachments = new HashMap<>(8);

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public List<String> getOwnerNamespaces() {
		return ownerNamespaces;
	}

	public void setOwnerNamespaces(List<String> ownerNamespaces) {
		this.ownerNamespaces = ownerNamespaces;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getJwt() {
		return jwt;
	}

	public void setJwt(String jwt) {
		this.jwt = jwt;
	}

	public PropertiesEnum.Role getRole() {
		return role;
	}

	public void setRole(PropertiesEnum.Role role) {
		this.role = role;
	}

	public boolean isRoleCorrectly(PropertiesEnum.Role target) {
		return 0 == target.compareTo(role);
	}

	@SuppressWarnings("all")
	public <T> T getAttachment(String key) {
		return (T) attachments.get(key);
	}

	@Override
	public String toString() {
		return "Privilege{" + "username='" + username + '\'' + ", ownerNamespaces='"
				+ ownerNamespaces + '\'' + ", jwt='" + jwt + '\'' + ", role=" + role
				+ '}';
	}
}
