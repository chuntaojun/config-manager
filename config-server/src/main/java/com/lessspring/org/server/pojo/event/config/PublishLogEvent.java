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
package com.lessspring.org.server.pojo.event.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Data
@Builder
@AllArgsConstructor
public class PublishLogEvent {

	public static final String TYPE = "PublishLogEvent";

	private long sequence;
	private String namespaceId;
	private String dataId;
	private String groupId;
	private String clientIp;
	private long publishTime;

	public PublishLogEvent() {
	}

}
