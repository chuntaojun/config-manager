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
package com.lessspring.org.server.utils;

import com.lessspring.org.model.vo.PublishConfigRequest;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class ConfigRequestUtils {

	public static byte[] getByte(PublishConfigRequest request) {
		byte[] save = null;
		if (request.isFile()) {
			byte[] file = request.getFile();
			save = ByteUtils.copyAndAdd(file, PropertiesEnum.ConfigType.FILE.getType(), 0,
					file.length + 1);
		}
		else {
			byte[] content = ByteUtils.toBytes(request.getContent());
			save = ByteUtils.copyAndAdd(content,
					PropertiesEnum.ConfigType.CONTENT.getType(), 0, content.length + 1);
		}
		return save;
	}

}
