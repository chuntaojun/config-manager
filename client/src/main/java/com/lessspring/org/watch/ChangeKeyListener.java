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
package com.lessspring.org.watch;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.lessspring.org.AbstractListener;
import com.lessspring.org.CacheConfigManager;
import com.lessspring.org.common.parser.ParserChain;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.pojo.ChangekeyEvent;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public abstract class ChangeKeyListener extends AbstractListener {

	private CacheConfigManager configManager;

	@Override
	public void onReceive(ConfigInfo configInfo) {
		ConfigInfo oldInfo = configManager.query(configInfo.getGroupId(),
				configInfo.getDataId(), configInfo.getEncryption());
		Map<String, Object> changeKeys = doCompare(configInfo, oldInfo);
		onChange(new ChangekeyEvent(changeKeys));
	}

	/**
	 * To monitor changes in the configuration part
	 *
	 * @param changekeyEvent {@link ChangekeyEvent}
	 */
	public abstract void onChange(ChangekeyEvent changekeyEvent);

	/**
	 * compute the old config-info and new config-info has transfer
	 * 
	 * @param newInfo new config-info
	 * @param oldInfo old config-info
	 * @return save the key has changed
	 */
	private Map<String, Object> doCompare(ConfigInfo newInfo, ConfigInfo oldInfo) {
		ParserChain chain = ParserChain.getInstance();
		Map<String, Object> oldMap = chain.toMap(oldInfo);
		Map<String, Object> newMap = chain.toMap(newInfo);
		return newMap.entrySet().stream()
				.filter(item -> !oldMap.containsKey(item.getKey())
						|| !Objects.equals(item.getValue(), oldMap.get(item.getKey())))
				.collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()),
						LinkedHashMap::putAll);
	}

}
