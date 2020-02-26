package com.conf.org;

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
                int k = 100;
                while (!finish || k -- != 0) {
                    if (lock.tryReadLock()) {
                        System.out.println("read tryReadLock " + Thread.currentThread().getName() + " : " + lock);
                        inWrite = false;
                        try {
                            count[0]++;
                        } finally {
                            System.out.println("read unReadLock "  + Thread.currentThread().getName() +  " : " + lock);
                            lock.unReadLock();
                        }
                    } else {
                        System.out.println(Thread.currentThread().getName() + " 获取读锁失败，" + lock);
                    }
                }
                finish = true;
            });
        }

        executorService.execute(() -> {
            for (int i = 0 ; i < 10 ; i ++) {
                if (lock.tryWriteLock()) {
                    System.out.println("write tryWriteLock " + Thread.currentThread().getName() + " : " + lock);
                    try {
                        inWrite = true;
                        for (int j = 0; j < 3; j++) {
                            Assert.assertTrue(inWrite);
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                        finish = true;
                        i = 1000 + 1;
                    } finally {
                        System.out.println("write unWriteLock : " + lock);
                        lock.unWriteLock();
                    }
                } else {
                    System.out.println(Thread.currentThread().getName() + " 获取写锁失败，" + lock);
                }
            }
            finish = true;
        });

        while (!finish) {}

    }

}