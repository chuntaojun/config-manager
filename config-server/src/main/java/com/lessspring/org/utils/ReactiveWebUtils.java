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

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.web.reactive.function.server.ServerRequest;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class ReactiveWebUtils {

	public static InetSocketAddress ALL_IP = new InetSocketAddress("0.0.0.0", 0);

	public static Optional<Object> getAttribute(String name, ServerRequest request) {
		return request.attribute(name);
	}

	private static Supplier<String> getselfIp() {
		return () -> "";
	}

}
