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
package com.lessspring.org.utils.vo;

import com.lessspring.org.db.dto.NamespaceDTO;
import com.lessspring.org.pojo.vo.NamespaceVO;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class NamespaceVOUtils {

	private NamespaceVOUtils() {
	}

	public static NamespaceVO change(NamespaceDTO dto) {
		return NamespaceVO.builder().namespaceName(dto.getNamespace())
				.namespaceId(dto.getNamespaceId()).build();
	}

}
