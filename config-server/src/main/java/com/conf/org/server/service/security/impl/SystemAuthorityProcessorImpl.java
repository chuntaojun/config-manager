package com.conf.org.server.service.security.impl;

import com.conf.org.server.service.security.AuthorityProcessor;
import com.conf.org.server.pojo.Privilege;
import com.conf.org.server.utils.PropertiesEnum;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019-11-24 23:11
 */
@Slf4j
@Service(value = "systemAuthorityProcessorImpl")
public class SystemAuthorityProcessorImpl implements AuthorityProcessor {

	@Override
	public boolean hasAuth(Privilege privilege, PropertiesEnum.Role[] roles) {
		return false;
	}

}
