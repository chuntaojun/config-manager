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

package com.lessspring.org.stream;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019-11-28 16:45
 */
public final class CStreamBuilder {

    private static final Logger logger = Logger
            .getLogger("com.lessspring.org.utils.StreamUtils");


    private static final ForkJoinPool myPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

    /**
     * Open {@link Stream <E>} of the collection in Java8, run by default to open parallel streams
     *
     * @param collection    target collection
     * @param <E>           type
     * @return {@link Stream<E>}
     */

    public static <E> Stream<E> openStream(Collection<E> collection) {
        return openStream(collection, true);
    }

    /**
     * Open the {@link Stream<E>} Stream of the collection in Java8, optionally opened according to allowParallel
     * Parallel streams can be turned on by default for collection sizes <value>16</value>
     *
     * @param collection    target collection
     * @param allowParallel parallel label
     * @param <E>           type
     * @return
     */

    public static <E> Stream<E> openStream(Collection<E> collection, boolean allowParallel) {
        return openStream(collection, allowParallel, 16);
    }

    /**
     * Open the {@link Stream<E>} Stream of the collection in Java8 and have free control over whether parallel Stream mode can be enabled
     *
     * @param collection    target collection
     * @param allowParallel parallel label
     * @param defaultSize   Set size threshold to enable parallel flows
     * @param <E>           type
     * @return {@link Stream<E>}
     */

    public static <E> Stream<E> openStream(Collection<E> collection, boolean allowParallel, int defaultSize) {
        int size = collection.size();
        if (allowParallel && size > defaultSize) {
            return collection.parallelStream();
        }
        return collection.stream();
    }

    /**
     * Put stream processing into the thread pool for execution, avoiding the global default {@link java.util.concurrent.ForkJoinPool}
     *
     * @param runnable Encapsulate the task to {@link Runnable}，for Stream processing without result aggregation
     */
    public static void invokeAsync(Runnable runnable) {
        myPool.execute(runnable);
    }

    /**
     * Put stream processing into the thread pool for execution, avoiding the global default {@link java.util.concurrent.ForkJoinPool}
     *
     * @param callable Encapsulate the task to {@link java.util.concurrent.Callable}，for Stream processing with result aggregation
     * @param <R>      return type
     * @return {@link Future <R>}
     */
    public static <R> Future<R> invokeAsync(Callable<R> callable) {
        return myPool.submit(callable);
    }

}
