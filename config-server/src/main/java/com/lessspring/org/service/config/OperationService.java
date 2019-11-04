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
package com.lessspring.org.service.config;

import com.lessspring.org.model.vo.DeleteConfigRequest;
import com.lessspring.org.model.vo.PublishConfigRequest;
import com.lessspring.org.model.vo.QueryConfigRequest;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.pojo.request.DeleteConfigHistory;
import com.lessspring.org.pojo.request.PublishConfigHistory;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public interface OperationService {

	ResponseData<?> queryConfig(String namespaceId, QueryConfigRequest request);

	ResponseData<?> publishConfig(String namespaceId, PublishConfigRequest request);

	ResponseData<?> modifyConfig(String namespaceId, PublishConfigRequest request);

	ResponseData<?> removeConfig(String namespaceId, DeleteConfigRequest request);

	ResponseData<?> saveConfigHistory(String namespaceId, PublishConfigHistory request);

	ResponseData<?> removeConfigHistory(String namespaceId, DeleteConfigHistory request);
}
