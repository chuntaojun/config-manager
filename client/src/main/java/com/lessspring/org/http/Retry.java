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
package com.lessspring.org.http;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public abstract class Retry<T> {

    /**
     * run work
     *
     * @throws Exception
     * @return Tasks are completed correctly
     */
    protected abstract T run() throws Exception;

    public T work() {
        int maxRetryNum = maxRetry();
        T data = null;
        while (maxRetryNum > 0) {
            try {
                data = run();
                if (shouldRetry(data, null)) {
                    maxRetryNum --;
                    continue;
                }
                return data;
            } catch (Throwable throwable) {
                if (!shouldRetry(null, throwable)) {
                    throw new UnSupportRetryException(throwable);
                }
                maxRetryNum --;
            }
        }
        throw new MaxRetryException("Has reached its maximum retries");
    }

    /**
     * Retry strategy
     *
     * @param data receive data
     * @param throwable if has throwable
     * @return Void
     */
    protected abstract boolean shouldRetry(T data, Throwable throwable);

    /**
     * The maximum number of retries
     *
     * @return number
     */
    protected abstract int maxRetry();

}
