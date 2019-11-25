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
package com.lessspring.org.exception;

import com.lessspring.org.constant.Code;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class DiskException extends BaseException {

	private static final long serialVersionUID = -1446108721154799171L;

	public DiskException() {
		super();
	}

	public DiskException(String message) {
		super(message);
	}

	public DiskException(String message, Throwable cause) {
		super(message, cause);
	}

	public DiskException(Throwable cause) {
		super(cause);
	}

	protected DiskException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	@Override
	public Code code() {
		return Code.DISK_OVERFLOW;
	}
}
