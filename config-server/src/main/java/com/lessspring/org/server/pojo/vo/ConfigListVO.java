package com.lessspring.org.server.pojo.vo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019/12/13 5:21 下午
 */
@Builder
@Data
@AllArgsConstructor
public class ConfigListVO {

	private Long page;
	private Long pageSize;
	private Long lastId;
	private List<ListItemVO> itemVOS;

	public ConfigListVO() {
	}
}
