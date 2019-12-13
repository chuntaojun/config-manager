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

package com.lessspring.org.service.encryption;

import com.lessspring.org.model.vo.KmsKeyRequest;
import com.lessspring.org.model.vo.KmsKeyResponse;
import com.lessspring.org.model.vo.ResponseData;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019-11-23 21:08
 */
public interface KmsSecurityKeyService {

	/**
	 * create kms key
	 *
	 * @param request {@link KmsKeyRequest}
	 * @return create operation label
	 */
	ResponseData<Boolean> createSecretKey(KmsKeyRequest request);

	/**
	 * query kms key
	 *
	 * @param request {@link KmsKeyRequest}
	 * @return {@link KmsKeyResponse}
	 */
	ResponseData<KmsKeyResponse> queryKmsKey(KmsKeyRequest request);

	/**
	 * clean secret key
	 *
	 * @param request {@link KmsKeyRequest}
	 * @return clean operation label
	 */
	ResponseData<Boolean> deleteSecretKey(KmsKeyRequest request);

}
