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
package com.lessspring.org.api;

import com.lessspring.org.constant.StringConst;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class ApiConstant {

	public static final String LOGIN = StringConst.API_V1 + "login";

	public static final String PUBLISH_CONFIG = StringConst.API_V1 + "publish/config";

	public static final String UPDATE_CONFIG = StringConst.API_V1 + "update/config";

	public static final String DELETE_CONFIG = StringConst.API_V1 + "delete/config";

	public static final String QUERY_CONFIG = StringConst.API_V1 + "query/config";

	public static final String WATCH_CONFIG = StringConst.API_V1 + "watch";

	public static final String WATCH_CONFIG_LONG_POLL = StringConst.API_V1 + "watch/longPoll";

	public static final String CLUSTER_NODE_JOIN = StringConst.API_V1 + "cluster/join";

	public static final String CLUSTER_NODE_LEAVE = StringConst.API_V1 + "cluster/leave";

	public static final String REFRESH_CLUSTER_NODE_INFO = StringConst.API_V1
			+ "cluster/all";

	public static final String DISTRO_SYNC_ALL = StringConst.API_V1 + "distro/sync/all";

	public static final String DISTRO_SYNC_NODE = StringConst.API_V1 + "distro/sync/node";

	public static final String DISTRO_CHECK = StringConst.API_V1 + "distro/sync/check";

}
