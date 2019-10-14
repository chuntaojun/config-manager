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
package com.lessspring.org.constant;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public enum Code {
    /**
     * success
     */
    SUCCESS(200, "Success"),

    /**
     * failure
     */
    FAILURE(500, "Failure"),

    /**
     * inner error
     */
    ERROR(-1, "Inner Error"),

    /**
     * unauthorized
     */
    UNAUTHORIZED(401, "Unauthorized"),

    /**
     * not found
     */
    NOT_FOUND(404, "not found"),

    /**
     * redirect
     */
    REDIRECT(503, "Redirect"),

    /**
     * hash no privilege
     */
    HASH_NO_PRIVILEGE(1001, "no privilege"),

    /**
     * need login
     */
    NEED_LOGIN(1002, "need login"),

    /**
     * user not found
     */
    USER_NOT_FOUNT(1003, "User not found"),

    /**
     * server busy
     */
    SERVER_BUSY(2001, "Server busy"),

    /**
     * Disk Overflow
     */
    DISK_OVERFLOW(4001, "Disk Overflow"),

    VERIFY_ERROR(5001, "Verify error")

    ;

    private final int code;
    private final String msg;

    Code(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
