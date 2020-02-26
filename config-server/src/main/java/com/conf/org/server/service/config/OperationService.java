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
package com.conf.org.server.service.config;

import com.conf.org.model.dto.ConfigInfo;
import com.conf.org.model.vo.ConfigQueryPage;
import com.conf.org.model.vo.DeleteConfigRequest;
import com.conf.org.model.vo.PublishConfigRequest;
import com.conf.org.model.vo.QueryConfigRequest;
import com.conf.org.model.vo.ResponseData;
import com.conf.org.server.pojo.request.DeleteConfigHistory;
import com.conf.org.server.pojo.vo.ConfigDetailVO;
import com.conf.org.server.pojo.vo.ConfigListVO;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public interface OperationService {

	/**
	 * query {@link ConfigInfo} to client
	 *
	 * @param namespaceId namespaceId
	 * @param request {@link QueryConfigRequest}
	 * @return ResponseData<?>
	 */
	ResponseData<?> queryConfig(String namespaceId, QueryConfigRequest request);

	/**
	 * publish config
	 *
	 * @param namespaceId namespaceId
	 * @param request {@link PublishConfigRequest}
	 * @return ResponseData<?>
	 */
	ResponseData<?> publishConfig(String namespaceId, PublishConfigRequest request);

	/**
	 * update config
	 *
	 * @param namespaceId namespaceId
	 * @param request {@link PublishConfigRequest}
	 * @return ResponseData<?>
	 */
	ResponseData<?> modifyConfig(String namespaceId, PublishConfigRequest request);

	/**
	 * delete config
	 *
	 * @param namespaceId namespaceId
	 * @param request {@link DeleteConfigRequest}
	 * @return ResponseData<?>
	 */
	ResponseData<?> removeConfig(String namespaceId, DeleteConfigRequest request);

	/**
	 * delete config-history
	 *
	 * @param namespaceId namespaceId
	 * @param request {@link DeleteConfigHistory}
	 * @return ResponseData<?>
	 */
	ResponseData<?> removeConfigHistory(String namespaceId, DeleteConfigHistory request);

	/**
	 * show config-list to server-page
	 *
	 * @param queryPage {@link ConfigQueryPage}
	 * @return {@link ResponseData<ConfigListVO>}
	 */
	ResponseData<ConfigListVO> configList(ConfigQueryPage queryPage);

	/**
	 *
	 * @param namespaceId namespaceId
	 * @param groupId groupId
	 * @param dataId dataId
	 * @return {@link ResponseData<ConfigDetailVO>}
	 */
	ResponseData<ConfigDetailVO> configDetail(String namespaceId, String groupId,
			String dataId);
}
