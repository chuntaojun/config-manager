package com.conf.org.executor;

import java.util.concurrent.ForkJoinPool;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019-12-01 14:33
 */
public class CForkJoinPool extends ForkJoinPool {

	private static final ForkJoinWorkerThreadFactory FACTORY = new CForkJoinThreadFactory();

	public CForkJoinPool() {
	}

	public CForkJoinPool(int parallelism) {
		super(parallelism);
	}

	public CForkJoinPool(int parallelism, Thread.UncaughtExceptionHandler handler,
			boolean asyncMode) {
		super(parallelism, FACTORY, handler, asyncMode);
	}

}
