package com.lessspring.org.executor;

/**
 * when thread execute job happen exception will call back to here
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019-11-20 16:09
 */
public class BaseUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

	@Override
	public void uncaughtException(Thread t, Throwable e) {

	}
}
