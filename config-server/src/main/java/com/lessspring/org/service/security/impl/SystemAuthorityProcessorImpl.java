package com.lessspring.org.service.security.impl;

import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.pojo.Privilege;
import com.lessspring.org.service.security.AuthorityProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019-11-24 23:11
 */
@Slf4j
@Service(value = "systemAuthorityProcessorImpl")
public class SystemAuthorityProcessorImpl implements AuthorityProcessor {

    @Override
    public boolean hasAuth(Privilege privilege) {
        return false;
    }

    @Override
    public ResponseData<?> createAuth(String namespaceId) {
        return null;
    }
}
