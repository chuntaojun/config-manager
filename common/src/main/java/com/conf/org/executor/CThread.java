package com.conf.org.executor;

import com.conf.org.context.TraceContext;
import com.conf.org.context.TraceContextHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.MDC;

/**
 * 自定义的线程对象，携带{@link TraceContext}
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019-12-01 14:52
 */
public class CThread extends Thread {

    private TraceContext traceContext;

    public CThread() {
        super();
        init();
    }

    public CThread(Runnable target) {
        super(target);
        init();
    }

    public CThread(@Nullable ThreadGroup group, Runnable target) {
        super(group, target);
        init();
    }

    public CThread(@NotNull String name) {
        super(name);
        init();
    }

    public CThread(@Nullable ThreadGroup group, @NotNull String name) {
        super(group, name);
        init();
    }

    public CThread(Runnable target, String name) {
        super(target, name);
        init();
    }

    public CThread(@Nullable ThreadGroup group, Runnable target, @NotNull String name) {
        super(group, target, name);
        init();
    }

    public CThread(@Nullable ThreadGroup group, Runnable target, @NotNull String name, long stackSize) {
        super(group, target, name, stackSize);
        init();
    }

    public TraceContext getTraceContext() {
        return traceContext;
    }

    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    public void cleanTraceContext() {
        traceContext.clean();
        MDC.remove("TraceId");
    }

    private void init() {
        traceContext = TraceContextHolder.getInstance().getInvokeTraceContext();
        MDC.put("TraceId", traceContext.getTraceId());
    }
}
