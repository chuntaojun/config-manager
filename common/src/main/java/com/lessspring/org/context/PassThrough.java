package com.lessspring.org.context;

import com.lessspring.org.executor.CForkJoinThread;
import com.lessspring.org.executor.CThread;
import org.slf4j.MDC;

import java.lang.reflect.Field;

/**
 * 该
 *
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019-11-28 16:48
 */
public class PassThrough {

    private static Field field;

    static {
        try {
            field = Thread.class.getDeclaredField("threadLocals");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private final TraceContext context;
    private final Thread source;

    public PassThrough() {
        this.context = TraceContextHolder.getInstance().getInvokeTraceContext();
        this.source = Thread.currentThread();
    }

    protected void transfer() {
        MDC.put("traceId", context.getTraceId());
        Thread currentThread = Thread.currentThread();
        if (currentThread instanceof CForkJoinThread) {
            CForkJoinThread joinThread = (CForkJoinThread) currentThread;
            joinThread.setTraceContext(context);
            return;
        }
        if (currentThread instanceof CThread) {
            CThread cThread = (CThread) currentThread;
            cThread.setTraceContext(context);
            return;
        }
        swapThreadLocal(currentThread, source);
    }

    protected void clean() {
        Thread currentThread = Thread.currentThread();
        if (currentThread instanceof CForkJoinThread) {
            CForkJoinThread joinThread = (CForkJoinThread) currentThread;
            joinThread.cleanTraceContext();
        }
        if (currentThread instanceof CThread) {
            CThread cThread = (CThread) currentThread;
            cThread.cleanTraceContext();
        }
    }

    private static void swapThreadLocal(Thread target, Thread source) {
        try {
            Object targetValue = field.get(source);
            field.set(target, targetValue);
        } catch (Exception ignore) {

        }
    }


}
