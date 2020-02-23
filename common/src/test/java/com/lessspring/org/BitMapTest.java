/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lessspring.org;

import com.googlecode.javaewah.EWAHCompressedBitmap;
import org.junit.Test;
import org.roaringbitmap.RoaringBitmap;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class BitMapTest {

    static int totalSize = 5000;

    private static final CountDownLatch LATCH = new CountDownLatch(32);

    private ExecutorService executorService = Executors.newFixedThreadPool(32);

    @Test
    public void test_for_large_bit_map_to_get() throws InterruptedException {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        int[] arrays = new int[totalSize];
        int[] query = new int[totalSize];

        for (int i = 0; i < totalSize; i++) {
            arrays[i] = Math.abs(Math.abs(random.nextInt()));
            query[i] = Math.abs(Math.abs(random.nextInt()));
        }

        on_ewah(arrays, query);
        on_roaring(arrays, query);

        LATCH.await();
    }

    private void on_ewah(int[] arrays, int[] query) {

        for (int c = 0; c < 16; c ++) {
            executorService.execute(() -> {
                EWAHCompressedBitmap bitmap = new EWAHCompressedBitmap();

                for (int i = 0; i < totalSize; i++) {
                    bitmap.set(query[i]);
                }

                long startTime = System.currentTimeMillis();

                for (int i = 0; i < totalSize; i++) {

                    int index = arrays[i];

                    if (bitmap.get(index)) {
                        bitmap.clear(index);
                    }
                }
                System.out.println("spend ewah [" + (System.currentTimeMillis() - startTime) + "] ms");
                LATCH.countDown();
            });
        }

    }

    private void on_roaring(int[] arrays, int[] query) {

        for (int c = 0; c < 16; c++) {
            executorService.execute(() -> {

                RoaringBitmap bitmap = new RoaringBitmap();


                for (int i = 0; i < totalSize; i++) {
                    bitmap.add(query[i]);
                }

                long startTime = System.currentTimeMillis();

                for (int i = 0; i < totalSize; i++) {

                    int index = arrays[i];

                    if (bitmap.contains(index)) {
                        bitmap.checkedRemove(index);
                    }
                }
                System.out.println("spend roaring [" + (System.currentTimeMillis() - startTime) + "] ms");
                LATCH.countDown();
            });
        }
    }

}
