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
package com.lessspring.org.utils;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class PropertiesEnum {

	public enum Bz {

		/**
		 * 
		 */
		NAMESPACE,

		/**
		 * 
		 */
		CONFIG,

		/**
		 * 
		 */
		USER,

		/**
		 * 
		 */
		SETTING,

		/**
		 *
		 */
		ID

	}

	public enum EmailType {

		/**
		 * info email label
		 */
		INFO(0, "info email"),

		/**
		 * warn email label
		 */
		WARN(1, "warn email"),

		/**
		 * error email label
		 */
		ERROR(-1, "error email");

		int code;
		String desc;

		EmailType(int code, String desc) {
			this.code = code;
			this.desc = desc;
		}

		public int getCode() {
			return code;
		}

		public String getDesc() {
			return desc;
		}
	}

	public enum Role {

		/**
		 * admin
		 */
		ADMIN((short) 0),

		/**
		 * user
		 */
		CUSTOMER((short) 1),

		/**
		 * developer
		 */
		DEVELOPER((short) 2),

		/**
		 * tester
		 */
		TESTER((short) 3);

		private short type;

		Role(short type) {
			this.type = type;
		}

		public static Role choose(short type) {
			if (type == (short) 0) {
				return Role.ADMIN;
			}
			if (type == (short) 1) {
				return Role.CUSTOMER;
			}
			if (type == (short) 2) {
				return Role.DEVELOPER;
			}
			if (type == (short) 3) {
				return Role.TESTER;
			}
			throw new IllegalArgumentException("Illegal user roles");
		}

		public short getType() {
			return type;
		}
	}

	public enum ConfigType {

		/**
		 * config type is file source stream
		 */
		FILE((byte) 1),

		/**
		 * config type is string
		 */
		CONTENT((byte) 0)

		;

		private byte type;

		ConfigType(byte type) {
			this.type = type;
		}

		public byte getType() {
			return type;
		}
	}

	public enum InterestKey {

		/**
		 * META-DATA
		 */
		META_DATA("META-DATA-"),

		/**
		 * CONFIG-DATA
		 */
		CONFIG_DATA("CONFIG-DATA"),

		/**
		 * USER-DATA
		 */
		USER_DATA("USER-DATA"),

		/**
		 * CONFIGURATION-DATA
		 */
		CONFIGURATION_DATA("CONFIGURATION-DATA"),;

		private String type;

		InterestKey(String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}
	}

	public enum ConfigStatus {

		/**
		 * SAVE
		 */
		SAVE(0),

		/**
		 * PUBLISH
		 */
		PUBLISH(1)

		;

		private int status;

		ConfigStatus(int status) {
			this.status = status;
		}

		public int getStatus() {
			return status;
		}
	}

	public enum Hint {

		/**
		 *
		 */
		HASH_NO_PRIVILEGE("no privilege"),

		/**
		 *
		 */
		NEED_LOGIN("need login"),

		;

		private String describe;

		Hint(String describe) {
			this.describe = describe;
		}

		public String getDescribe() {
			return describe;
		}
	}

	public enum Jwt {

		/**
		 *
		 */
		TOKEN_STATUS_EXPIRE(-1, "token had expire"),

		/**
		 *
		 */
		TOKEN_STATUS_REFRESH(0, "token should be refresh"),

		/**
		 *
		 */
		TOKEN_STATUS_HEALTH(1, "token is alive"),

		/**
		 *
		 */
		TOKEN_EXPIRE_RANGE(10);

		private int value;
		private String doc;

		Jwt(int value) {
			this.value = value;
		}

		Jwt(int value, String doc) {
			this.value = value;
			this.doc = doc;
		}

		public int getValue() {
			return value;
		}

		public String getDoc() {
			return doc;
		}
	}
}
