package com.lessspring.org.executor;

import com.lessspring.org.context.TraceContext;
import com.lessspring.org.context.TraceContextHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019-12-01 14:52
 */
public class CThread extends Thread {

    private TraceContext traceContext = new TraceContext();

    public CThread() {
        super();
        traceContext = TraceContextHolder.getInstance().getInvokeTraceContext();
    }

    public CThread(Runnable target) {
        super(target);
        traceContext = TraceContextHolder.getInstance().getInvokeTraceContext();
    }

    public CThread(@Nullable ThreadGroup group, Runnable target) {
        super(group, target);
        traceContext = TraceContextHolder.getInstance().getInvokeTraceContext();
    }

    public CThread(@NotNull String name) {
        super(name);
        traceContext = TraceContextHolder.getInstance().getInvokeTraceContext();
    }

    public CThread(@Nullable ThreadGroup group, @NotNull String name) {
        super(group, name);
        traceContext = TraceContextHolder.getInstance().getInvokeTraceContext();
    }

    public CThread(Runnable target, String name) {
        super(target, name);
        traceContext = TraceContextHolder.getInstance().getInvokeTraceContext();
    }

    public CThread(@Nullable ThreadGroup group, Runnable target, @NotNull String name) {
        super(group, target, name);
        traceContext = TraceContextHolder.getInstance().getInvokeTraceContext();
    }

    public CThread(@Nullable ThreadGroup group, Runnable target, @NotNull String name, long stackSize) {
        super(group, target, name, stackSize);
        traceContext = TraceContextHolder.getInstance().getInvokeTraceContext();
    }

    public TraceContext getTraceContext() {
        return traceContext;
    }

    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    public void cleanTraceContext() {
        traceContext = null;
    }
}
