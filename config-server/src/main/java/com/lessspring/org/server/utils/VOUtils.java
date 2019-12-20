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

import com.lessspring.org.db.dto.ConfigBetaInfoDTO;
import com.lessspring.org.db.dto.ConfigInfoDTO;
import com.lessspring.org.db.dto.NamespaceDTO;
import com.lessspring.org.db.dto.UserDTO;
import com.lessspring.org.raft.pojo.ServerNode;
import com.lessspring.org.raft.vo.ServerNodeVO;
import com.lessspring.org.server.pojo.vo.ConfigDetailVO;
import com.lessspring.org.server.pojo.vo.ConfigListVO;
import com.lessspring.org.server.pojo.vo.ListItemVO;
import com.lessspring.org.server.pojo.vo.NamespaceVO;
import com.lessspring.org.server.pojo.vo.UserVO;
import com.lessspring.org.server.pojo.vo.WatchClientVO;
import com.lessspring.org.server.service.publish.client.WatchClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019/12/15 2:50 下午
 */
public final class VOUtils {

	public static ServerNodeVO convertServerNodeVO(long id, ServerNode node) {
		return ServerNodeVO.builder().id(id).ip(node.getKey()).role(node.getRole())
				.status(node.getServerStatus().name()).remote("http://" + node.getKey())
				.build();
	}

	public static NamespaceVO convertNamespaceVO(NamespaceDTO dto) {
		return NamespaceVO.builder().namespaceName(dto.getNamespace())
				.namespaceId(dto.getNamespaceId()).build();
	}

	public static ConfigDetailVO convertToConfigDetailVO(ConfigInfoDTO configInfoDTO) {
		ConfigDetailVO vo = ConfigDetailVO.builder()
				.namespaceId(configInfoDTO.getNamespaceId())
				.groupId(configInfoDTO.getGroupId()).dataId(configInfoDTO.getDataId())
				.content(configInfoDTO.getContent())
				.encryption(configInfoDTO.getEncryption()).type(configInfoDTO.getType())
				.remark(configInfoDTO.getRemark()).build();
		return vo;
	}

	public static ConfigDetailVO convertToConfigDetailVO(ConfigBetaInfoDTO betaInfoDTO) {
		ConfigDetailVO vo = ConfigDetailVO.builder()
				.namespaceId(betaInfoDTO.getNamespaceId())
				.groupId(betaInfoDTO.getGroupId()).dataId(betaInfoDTO.getDataId())
				.clientIps(betaInfoDTO.getClientIps()).content(betaInfoDTO.getContent())
				.encryption(betaInfoDTO.getEncryption()).type(betaInfoDTO.getType())
				.remark(betaInfoDTO.getRemark()).build();
		return vo;
	}

	public static ConfigListVO convertToConfigListVO(List<Map<String, String>> list) {
		list.sort((o1, o2) -> (int) (Long.parseLong(o1.get("id"))
				- Long.parseLong(o2.get("id"))));
		ConfigListVO vo = new ConfigListVO();
		List<ListItemVO> listItemVOS = new ArrayList<>();
		for (Map<String, String> item : list) {
			ListItemVO itemVO = new ListItemVO();
			itemVO.setGroupId(item.get("groupId"));
			itemVO.setDataId(item.get("dataId"));
			listItemVOS.add(itemVO);
		}
		vo.setLastId(Long.parseLong(list.get(list.size() - 1).get("id")));
		vo.setItemVOS(listItemVOS);
		return vo;
	}

	public static WatchClientVO convertWatchClientVO(WatchClient watchClient) {
		return WatchClientVO.builder().clientId(watchClient.getClientId())
				.clientIp(watchClient.getClientIp()).build();
	}

	public static UserVO convertUserVo(UserDTO dto) {
		return UserVO.builder().id(dto.getId()).username(dto.getUsername())
				.role(PropertiesEnum.Role.choose(dto.getRoleType()).name()).build();
	}

}
