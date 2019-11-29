package com.lessspring.org.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class CasReadWriteLockTest {

	static volatile boolean finish = false;
	static volatile boolean inWrite = true;

	@Test
    public void readWriteLockTest() {
        final CasReadWriteLock lock = new CasReadWriteLock();
        AtomicInteger readCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(14);

        for (int i = 0; i < 10; i ++) {
            executorService.execute(() -> {
                while (!finish) {
                    lock.tryReadLock();
                    try {
                        System.out.println("read work " + readCount.incrementAndGet());
                        inWrite = false;
                    } finally {
                        lock.unReadLock();
                    }
                }
            });
        }

        executorService.execute(() -> {
            for (int i = 0 ; i < 1000 ; i ++) {
                lock.tryWriteLock();
                try {
                    inWrite = true;
                    for (int j = 0; j < 3; j ++) {
                        System.out.println("write work " + j);
                        Assert.assertTrue(inWrite);
                    }
                } finally {
                    lock.unWriteLock();
                }
            }
            finish = true;
        });

        while (!finish) {}

    }

}