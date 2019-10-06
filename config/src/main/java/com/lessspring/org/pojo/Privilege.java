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

import com.lessspring.org.utils.PropertiesEnum;

/**
 * The custom of a simple access objects
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class Privilege {

    private String username;
    private String ownerNamespace;
    private transient String jwt;
    private PropertiesEnum.Role role;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOwnerNamespace() {
        return ownerNamespace;
    }

    public void setOwnerNamespace(String ownerNamespace) {
        this.ownerNamespace = ownerNamespace;
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

}
