package com.lessspring.org.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.PriorityOrdered;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019-11-28 20:51
 */
public class TraceActuator implements PriorityOrdered {

    @Pointcut(value = "@annotation(com.lessspring.org.configuration.trace.NeedTrace)")
    private void trace() {
    }

    @Around("trace()")
    public Object aroundService(ProceedingJoinPoint pjp) throws Throwable {
        return pjp.proceed();
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
