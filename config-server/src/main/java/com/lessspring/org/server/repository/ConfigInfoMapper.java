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

import com.lessspring.org.db.dto.ConfigBetaInfoDTO;
import com.lessspring.org.db.dto.ConfigInfoDTO;
import com.lessspring.org.model.vo.DeleteConfigRequest;
import com.lessspring.org.server.pojo.query.QueryConfigInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Mapper
public interface ConfigInfoMapper {

	/**
	 * query config-info by {@link QueryConfigInfo}
	 *
	 * @param queryConfigInfo query config-info param
	 * @return config-content
	 */
	String findConfigInfoContent(@Param(value = "query") QueryConfigInfo queryConfigInfo);

	/**
	 * query config-info by {@link QueryConfigInfo}
	 *
	 * @param queryConfigInfo query config-info param
	 * @return {@link ConfigInfoDTO}
	 */
	ConfigInfoDTO findConfigInfo(@Param(value = "query") QueryConfigInfo queryConfigInfo);

	/**
	 * query config-beta-info by {@link QueryConfigInfo}
	 *
	 * @param queryConfigInfo query config-beta-info param
	 * @return {@link ConfigBetaInfoDTO}
	 */
	ConfigBetaInfoDTO findConfigBetaInfo(
			@Param(value = "query") QueryConfigInfo queryConfigInfo);

	/**
	 * find min and max id
	 *
	 * @return {@link List<Long>}
	 */
	List<Long> findMinAndMaxId();

	/**
	 * batch find config info by ids
	 *
	 * @param ids ids
	 * @return {@link List<ConfigInfoDTO>}
	 */
	List<ConfigInfoDTO> batchFindConfigInfo(@Param("ids") List<Long> ids);

	/**
	 * find min and max id
	 *
	 * @return {@link List<Long>}
	 */
	List<Long> findMinAndMaxId4Beta();

	/**
	 * batch find config info by ids
	 *
	 * @param ids ids
	 * @return {@link List<ConfigInfoDTO>}
	 */
	List<ConfigBetaInfoDTO> batchFindConfigInfo4Beta(@Param("ids") List<Long> ids);

	/**
	 * save config-info into db
	 *
	 * @param dto {@link ConfigInfoDTO}
	 * @return Successful article number
	 */
	int saveConfigInfo(@Param(value = "dto") ConfigInfoDTO dto);

	/**
	 * update config-info into db
	 *
	 * @param dto {@link ConfigInfoDTO}
	 * @return Successful article number
	 */
	int updateConfigInfo(@Param(value = "dto") ConfigInfoDTO dto);

	/**
	 * save beta config-info into db
	 *
	 * @param dto {@link ConfigBetaInfoDTO}
	 * @return Successful article number
	 */
	int saveConfigBetaInfo(@Param(value = "dto") ConfigBetaInfoDTO dto);

	/**
	 * update beta config-info into db
	 *
	 * @param dto {@link ConfigBetaInfoDTO}
	 * @return Successful article number
	 */
	int updateConfigBetaInfo(@Param(value = "dto") ConfigBetaInfoDTO dto);

	/**
	 * clean config-info by {@link DeleteConfigRequest}
	 *
	 * @param queryConfigInfo {@link DeleteConfigRequest}
	 * @return clean result
	 */
	int removeConfigInfo(@Param(value = "dto") DeleteConfigRequest queryConfigInfo);

	/**
	 * clean beta config-info by {@link DeleteConfigRequest}
	 *
	 * @param queryConfigInfo {@link DeleteConfigRequest}
	 * @return clean result
	 */
	int removeConfigBetaInfo(@Param(value = "dto") DeleteConfigRequest queryConfigInfo);

}
