package com.lessspring.org.server.pojo.vo;

import com.lessspring.org.utils.GsonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019/12/13 5:24 下午
 */
@Builder
@Data
@AllArgsConstructor
public class ConfigDetailVO {

	private String namespaceId;
	private String groupId;
	private String dataId;
	private byte[] fileSource;
	private byte[] content;
	private String type;
	private String remark;
	private String clientIps;
	private String encryption = "";
	private Long createTime;
	private Integer status = 0;

	public ConfigDetailVO() {
	}

	@Override
	public String toString() {
		return GsonUtils.toJson(this);
	}
}
