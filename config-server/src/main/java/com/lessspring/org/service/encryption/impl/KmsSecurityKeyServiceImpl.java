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

package com.lessspring.org.service.encryption.impl;

import com.lessspring.org.model.vo.KmsKeyRequest;
import com.lessspring.org.model.vo.KmsKeyResponse;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.service.encryption.KmsSecurityKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019-11-23 21:08
 */
@Slf4j
@Service
public class KmsSecurityKeyServiceImpl implements KmsSecurityKeyService {
    @Override
    public ResponseData<Boolean> createSecretKey(KmsKeyRequest request) {
        return null;
    }

    @Override
    public ResponseData<KmsKeyResponse> queryKmsKey(KmsKeyRequest request) {
        return null;
    }

    @Override
    public ResponseData<Boolean> deleteSecretKey(KmsKeyRequest request) {
        return null;
    }
}
