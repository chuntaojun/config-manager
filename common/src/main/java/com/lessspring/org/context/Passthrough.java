package com.lessspring.org.context;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019-11-28 16:48
 */
public class Passthrough {

    private static final TraceContextHolder contextHolder = TraceContextHolder.getInstance();

    protected void transfer() {
    }

    protected void clean() {
        contextHolder.removeInvokeTraceContext();
    }

}
