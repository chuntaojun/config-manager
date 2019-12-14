package com.lessspring.org.server.pojo.request;

import java.util.List;

import lombok.Builder;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019/12/8 10:43 上午
 */
@Builder
public class IDRequest {

	private String localName;

	private List<SubIDRequest> subIDRequests;

	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}

	public List<SubIDRequest> getSubIDRequests() {
		return subIDRequests;
	}

	public void setSubIDRequests(List<SubIDRequest> subIDRequests) {
		this.subIDRequests = subIDRequests;
	}
}
