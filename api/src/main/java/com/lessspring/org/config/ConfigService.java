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
package com.lessspring.org.config;

import com.lessspring.org.AbstractListener;
import com.lessspring.org.LifeCycle;
import com.lessspring.org.model.dto.ConfigInfo;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public interface ConfigService extends LifeCycle {

	/**
	 * get config by groupId and dataId
	 *
	 * @param groupId groupId
	 * @param dataId dataId
	 * @return config {@link ConfigInfo}
	 */
	ConfigInfo getConfig(String groupId, String dataId);

	/**
	 * get config by groupId and dataId with encryption
	 *
	 * @param groupId groupId
	 * @param dataId dataId
	 * @param encryption encryption token
	 * @return config {@link ConfigInfo}
	 */
	ConfigInfo getConfig(String groupId, String dataId, String encryption);

	/**
	 * publish config
	 *
	 * @param groupId groupId
	 * @param dataId dataId
	 * @param content config content
	 * @param type config type, such as {properties, yml, yaml, json, xml}
	 * @return Release successful logo
	 */
	boolean publishConfig(String groupId, String dataId, String content, String type);

	/**
	 * publish config
	 *
	 * @param groupId groupId
	 * @param dataId dataId
	 * @param content config content
	 * @param type config type, such as {properties, yml, yaml, json, xml}
	 * @param encryption encryption token
	 * @return Release successful logo
	 */
	boolean publishConfig(String groupId, String dataId, String content, String type,
			String encryption);

	/**
	 * publish config file
	 *
	 * @param groupId groupId
	 * @param dataId dataId
	 * @param stream file byte stream
	 * @return Release successful logo
	 */
	boolean publishConfigFile(String groupId, String dataId, byte[] stream);

	/**
	 * delete config
	 *
	 * @param groupId groupId
	 * @param dataId dataId
	 * @return delete success sign
	 */
	boolean deleteConfig(String groupId, String dataId);

	/**
	 * Add configuration changes the listener
	 *
	 * @param groupId groupId
	 * @param dataId dataId
	 * @param listeners listener list
	 */
	void addListener(String groupId, String dataId, AbstractListener... listeners);

	/**
	 * Add configuration changes the listener
	 *
	 * @param groupId groupId
	 * @param dataId dataId
	 * @param encryption encryption token
	 * @param listeners listener list
	 */
	void addListener(String groupId, String dataId, String encryption,
			AbstractListener... listeners);

	/**
	 * Remove configuration changes the listener
	 *
	 * @param groupId groupId
	 * @param dataId dataId
	 * @param listeners listener list
	 */
	void removeListener(String groupId, String dataId, AbstractListener... listeners);

}
