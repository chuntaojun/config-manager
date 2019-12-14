package com.lessspring.org.server.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019/12/13 5:21 下午
 */
@Builder
@Data
@AllArgsConstructor
public class ConfigListVO {

    private long page;
    private long pageSize;
    private List<ListItemVO> itemVOS;

    public ConfigListVO() {
    }
}
