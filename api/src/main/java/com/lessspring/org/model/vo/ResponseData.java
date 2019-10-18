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
package com.lessspring.org.model.vo;

import com.lessspring.org.constant.Code;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since
 */
public class ResponseData<T> {

	private int code;
	private T data;
	private String errMsg;

	public int getCode() {
		return code;
	}

	public T getData() {
		return data;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public boolean ok() {
		return code == Code.SUCCESS.getCode() || code == 0;
	}

	public static ResponseData<Boolean> success() {
		return ResponseData.builder().withCode(200).withData(true).build();
	}

	public static <T> ResponseData<T> success(T data) {
		return ResponseData.builder().withCode(200).withData(data).build();
	}

	public static ResponseData<Boolean> fail(Throwable throwable) {
		return ResponseData.builder().withCode(500).withData(false)
				.withErrMsg(throwable.getMessage()).build();
	}

	public static <T> ResponseData<T> fail(Code code) {
		return ResponseData.builder().withCode(code.getCode()).withErrMsg(code.getMsg())
				.build();
	}

	public static <T> ResponseData<T> fail() {
		return ResponseData.builder().withCode(500).withErrMsg("failed").build();
	}

	public static <T> Builder builder() {
		return new Builder();
	}

	public static final class Builder<T> {
		private int code;
		private T data;
		private String errMsg;

		private Builder() {
		}

		public Builder withCode(int code) {
			this.code = code;
			return this;
		}

		public Builder withData(T data) {
			this.data = data;
			return this;
		}

		public Builder withErrMsg(String errMsg) {
			this.errMsg = errMsg;
			return this;
		}

		public ResponseData build() {
			ResponseData responseData = new ResponseData();
			responseData.data = this.data;
			responseData.errMsg = this.errMsg;
			responseData.code = this.code;
			return responseData;
		}
	}

}
