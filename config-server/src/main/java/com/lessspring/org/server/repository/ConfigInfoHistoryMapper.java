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
package com.lessspring.org.server.repository;

import java.util.List;

import com.lessspring.org.db.dto.ConfigInfoHistoryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Mapper
public interface ConfigInfoHistoryMapper {

	/**
	 * save {@link ConfigInfoHistoryDTO} to db
	 *
	 * @param historyDTO {@link ConfigInfoHistoryDTO}
	 * @return affect row
	 */
	int save(@Param("dto") ConfigInfoHistoryDTO historyDTO);

	/**
	 * batch delete history config-info
	 *
	 * @param ids config-history-id
	 * @return affect rows
	 */
	int batchDelete(@Param(value = "ids") List<Long> ids);

	/**
	 * find config-info-history min and max id
	 *
	 * @return min and max id
	 */
	List<Long> findMinAndMaxId();

}
