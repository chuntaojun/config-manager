package com.lessspring.org.utils;

import com.lessspring.org.ThreadUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

	private final AtomicInteger monitor = new AtomicInteger(IN_FREE_STATUS);

    private final AtomicBoolean inHappyCode = new AtomicBoolean(true);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

	public boolean tryReadLock() {
		return tryReadLock(MAX_RETRY_CNT);
	}

    public boolean tryReadLock(int retryCnt) {
		for (int i = 0; i < retryCnt; i++) {
		    if (!inHappyCode.get()) {
		        break;
            }
			if (monitor.compareAndSet(IN_FREE_STATUS, IN_READ_STATUS)
					|| monitor.get() == IN_READ_STATUS) {
			    inHappyCode.lazySet(true);
				return true;
			}
            ThreadUtils.sleep(1);
		}
		inHappyCode.lazySet(false);
        try {
            return readLock.tryLock(1000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
	}

	public boolean tryWriteLock() {
		return tryWriteLock(MAX_RETRY_CNT);
	}

    public boolean tryWriteLock(int retryCnt) {
		for (int i = 0; i < retryCnt; i++) {
            if (!inHappyCode.get()) {
                break;
            }
			if (monitor.compareAndSet(IN_FREE_STATUS, IN_WRITE_STATUS)) {
                inHappyCode.lazySet(true);
				return true;
			}
            ThreadUtils.sleep(1);
		}
        inHappyCode.lazySet(false);
        try {
            return writeLock.tryLock(1000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
	}

	public void unReadLock() {
		monitor.lazySet(IN_FREE_STATUS);
		readLock.unlock();
	}

    public void unWriteLock() {
        monitor.lazySet(IN_FREE_STATUS);
        writeLock.unlock();
    }

}
