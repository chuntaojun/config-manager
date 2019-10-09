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
package com.lessspring.org.raft.dto;

import com.lessspring.org.raft.OperationEnum;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class Datum {

    private String key;
    private byte[] value;
    private String className;
    private OperationEnum operationEnum;

    public Datum(String key, byte[] value, String className) {
        this.key = key;
        this.value = value;
        this.className = className;
    }

    public String getKey() {
        return key;
    }

    public byte[] getValue() {
        return value;
    }

    public String getClassName() {
        return className;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public OperationEnum getOperationEnum() {
        return operationEnum;
    }

    public void setOperationEnum(OperationEnum operationEnum) {
        this.operationEnum = operationEnum;
    }
}
