package com.lessspring.org.executor;

import com.lessspring.org.context.TraceContext;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019-12-01 14:30
 */
public class CForkJoinThread extends ForkJoinWorkerThread {

    private TraceContext traceContext = new TraceContext();

    /**
     * Creates a ForkJoinWorkerThread operating in the given pool.
     *
     * @param pool the pool this thread works in
     * @throws NullPointerException if pool is null
     */
    protected CForkJoinThread(ForkJoinPool pool) {
        super(pool);
    }

    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    public void cleanTraceContext() {
        traceContext = null;
    }

}
