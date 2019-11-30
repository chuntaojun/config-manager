package com.lessspring.org;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple read-write lock implementation
 *
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @since 0.0.1
 * @Created at 2019-11-23 12:56
 */
public class CasReadWriteLock {

	private static final short IN_READ_STATUS = 1;
	private static final short IN_FREE_STATUS = 0;
	private static final short IN_WRITE_STATUS = -1;

	private static final int MAX_RETRY_CNT = 10;

	private final AtomicInteger readCnt = new AtomicInteger(0);
	private volatile boolean inWrite = false;
	private final AtomicInteger monitor = new AtomicInteger(IN_FREE_STATUS);

	public boolean tryReadLock() {
		return tryReadLock(MAX_RETRY_CNT);
	}

	public boolean tryReadLock(int retryCnt) {
		// 等待写锁释放
		while (inWrite) {
			// none
		}
		for (int i = 0; i < retryCnt; i++) {
			if (monitor.compareAndSet(IN_FREE_STATUS, IN_READ_STATUS)
					|| monitor.get() == IN_READ_STATUS) {
				readCnt.incrementAndGet();
				return true;
			}
		}
		return false;
	}

	public boolean tryWriteLock() {
		return tryWriteLock(MAX_RETRY_CNT);
	}

	public boolean tryWriteLock(int retryCnt) {
		for (int i = 0; i < retryCnt; i++) {
			if (readCnt.get() == 0
					&& monitor.compareAndSet(IN_FREE_STATUS, IN_WRITE_STATUS)) {
				return true;
			}
		}
		// 强行抢占锁，接下来的读锁全部等待
		inWrite = true;
		// 等待读锁完全释放
		while (readCnt.get() != 0) {
			// none
		}
		for (int i = 0; i < retryCnt; i ++) {
			if (monitor.compareAndSet(IN_FREE_STATUS, IN_WRITE_STATUS)) {
				return true;
			}
		}
		return false;
	}

	public void unReadLock() {
		readCnt.decrementAndGet();
		monitor.lazySet(IN_FREE_STATUS);
	}

	public void unWriteLock() {
		inWrite = false;
		monitor.lazySet(IN_FREE_STATUS);
	}

}
