package com.lessspring.org;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * a simple compute the work execute time spend
 *
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019-11-15 16:34
 */
public final class TimerContext {

    public static class TimerContextNoArgsNoResult {

        private static final Logger logger = Logger.getLogger("com.lessspring.org.utils.TimerContextNoArgsNoResult");

        private final Runnable target;

        private TimerContextNoArgsNoResult(Runnable target) {
            this.target = target;
        }

        void run() {
            long startTime = System.currentTimeMillis();
            logger.info("[TimerContextNoArgsNoResult] start in : " + startTime);
            target.run();
            long endTime = System.currentTimeMillis();
            logger.info("[TimerContextNoArgsNoResult] end in : " + endTime + ", cost : " + (endTime - startTime) + " Ms");
        }

    }

    public static class TimerContextWithArgsNoResult<A> {

        private static final Logger logger = Logger.getLogger("com.lessspring.org.utils.TimerContextWithArgsNoResult");

        private final Consumer<A> consumer;

        private TimerContextWithArgsNoResult(Consumer<A> consumer) {
            this.consumer = consumer;
        }

        void accept(A a) {
            long startTime = System.currentTimeMillis();
            logger.info("[TimerContextWithArgsNoResult] start in : " + startTime);
            consumer.accept(a);
            long endTime = System.currentTimeMillis();
            logger.info("[TimerContextWithArgsNoResult] end in : " + endTime + ", cost : " + (endTime - startTime) + " Ms");
        }
    }

    public static class TimerContextNoArgsWithResult<R> {

        private static final Logger logger = Logger.getLogger("com.lessspring.org.utils.TimerContextNoArgsWithResult");

        private final Supplier<R> supplier;

        private TimerContextNoArgsWithResult(Supplier<R> supplier) {
            this.supplier = supplier;
        }

        R acquire() {
            R result;
            long startTime = System.currentTimeMillis();
            logger.info("[TimerContextWithArgsNoResult] start in : " + startTime);
            result = supplier.get();
            long endTime = System.currentTimeMillis();
            logger.info("[TimerContextWithArgsNoResult] end in : " + endTime + ", cost : " + (endTime - startTime) + " Ms");
            return result;
        }
    }

    public static class TimerContextWithArgsAndResult<A, R> {

        private static final Logger logger = Logger.getLogger("com.lessspring.org.utils.TimerContextWithArgsAndResult");

        private final Function<A, R> function;

        private TimerContextWithArgsAndResult(Function<A, R> function) {
            this.function = function;
        }

        R applt(A a) {
            R result;
            long startTime = System.currentTimeMillis();
            logger.info("[TimerContextWithArgsNoResult] start in : " + startTime);
            result = function.apply(a);
            long endTime = System.currentTimeMillis();
            logger.info("[TimerContextWithArgsNoResult] end in : " + endTime + ", cost : " + (endTime - startTime) + " Ms");
            return result;
        }
    }

    public static void invokeNoArgsNoResult(Runnable runnable) {
        TimerContextNoArgsNoResult context = new TimerContextNoArgsNoResult(runnable);
        context.run();
    }

    public static <A> void invokeWithArgsNoResult(Consumer<A> consumer, A args) {
        TimerContextWithArgsNoResult<A> context = new TimerContextWithArgsNoResult<>(consumer);
        context.accept(args);
    }

    public static <R> R invokeNoArgsWithResult(Supplier<R> supplier) {
        TimerContextNoArgsWithResult<R> context = new TimerContextNoArgsWithResult<>(supplier);
        return context.acquire();
    }

    public static <A, R> R invokeWithArgsWithResult(Function<A, R> function, A args) {
        TimerContextWithArgsAndResult<A, R> context = new TimerContextWithArgsAndResult<>(function);
        return context.applt(args);
    }

}
