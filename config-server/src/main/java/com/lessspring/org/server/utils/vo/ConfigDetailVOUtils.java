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

package com.lessspring.org.server.utils.vo;

import com.lessspring.org.db.dto.ConfigBetaInfoDTO;
import com.lessspring.org.db.dto.ConfigInfoDTO;
import com.lessspring.org.server.pojo.vo.ConfigDetailVO;
import com.lessspring.org.server.pojo.vo.ConfigListVO;
import com.lessspring.org.server.pojo.vo.ListItemVO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019/12/13 5:38 下午
 */
public final class ConfigDetailVOUtils {

    public static ConfigDetailVO convertToConfigDetailVO(ConfigInfoDTO configInfoDTO) {
        ConfigDetailVO vo = ConfigDetailVO.builder()
                .namespaceId(configInfoDTO.getNamespaceId())
                .groupId(configInfoDTO.getGroupId())
                .dataId(configInfoDTO.getDataId())
                .content(configInfoDTO.getContent())
                .encryption(configInfoDTO.getEncryption())
                .type(configInfoDTO.getType())
                .remark(configInfoDTO.getRemark())
                .build();
        return vo;
    }

    public static ConfigDetailVO convertToConfigDetailVO(ConfigBetaInfoDTO betaInfoDTO) {
        ConfigDetailVO vo = ConfigDetailVO.builder()
                .namespaceId(betaInfoDTO.getNamespaceId())
                .groupId(betaInfoDTO.getGroupId())
                .dataId(betaInfoDTO.getDataId())
                .clientIps(betaInfoDTO.getClientIps())
                .content(betaInfoDTO.getContent())
                .encryption(betaInfoDTO.getEncryption())
                .type(betaInfoDTO.getType())
                .remark(betaInfoDTO.getRemark())
                .build();
        return vo;
    }

    public static ConfigListVO convertToConfigListVO(List<Map<String, String>> list) {
        ConfigListVO vo = new ConfigListVO();
        List<ListItemVO> listItemVOS = new ArrayList<>();
        for (Map<String, String> item : list) {
            ListItemVO itemVO = new ListItemVO();
            itemVO.setGroupId(item.get("groupId"));
            itemVO.setDataId(item.get("dataId"));
            listItemVOS.add(itemVO);
        }
        vo.setItemVOS(listItemVOS);
        return vo;
    }

}
