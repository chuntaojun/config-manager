package com.lessspring.org.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CasReadWriteLockTest {

	static volatile boolean finish = false;
	static volatile boolean inWrite = true;

	@Test
    public void readWriteLockTest() {
        final CasReadWriteLock lock = new CasReadWriteLock();

        ExecutorService executorService = Executors.newFixedThreadPool(14);

        final int[] count = new int[1];

        for (int i = 0; i < 10; i ++) {
            executorService.execute(() -> {
                int k = 10_0000;
                while (!finish || k -- != 0) {
                    lock.tryReadLock();
                    try {
                        count[0] ++;
                        inWrite = false;
                    } finally {
                        lock.unReadLock();
                    }
                }
                finish = true;
            });
        }

        executorService.execute(() -> {
            for (int i = 0 ; i < 10 ; i ++) {
                lock.tryWriteLock();
                try {
                    inWrite = true;
                    for (int j = 0; j < 3; j ++) {
                        System.out.println("write work " + j);
                        Assert.assertTrue(inWrite);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                    finish = true;
                    i = 1000 + 1;
                } finally {
                    lock.unWriteLock();
                }
            }
            finish = true;
        });

        while (!finish) {}

    }

}