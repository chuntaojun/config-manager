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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.lessspring.org.DiskUtils;
import com.lessspring.org.PathUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Getter
public class PathConstants {

	@Value("${com.lessspring.org.config.manager.cache-dir:${user.home}/config-manager/server-${server.port}}")
	private String parentPath;

	@Value("${com.lessspring.org.config-manager.exitDeleteFile:false}")
	private boolean exitDeleteFile;

	@PostConstruct
	public void init() {
		PathUtils.init(parentPath);
	}

	@PreDestroy
	public void destroy() {
		if (exitDeleteFile) {
			log.warn(
					"In the exitDeleteFile mode, relevant files of config-manger will be deleted automatically");
			DiskUtils.deleteDir(parentPath);
		}
	}

}
