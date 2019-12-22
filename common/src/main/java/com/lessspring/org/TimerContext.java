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

import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * a simple compute the work execute time spend
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019-11-15 16:34
 */
public final class TimerContext {

		private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TimerContext.class);

		private static class TimerContextNoArgsNoResult {

		private final Runnable target;

		private TimerContextNoArgsNoResult(Runnable target) {
			this.target = target;
		}

		void run() {
			long startTime = System.currentTimeMillis();
			logger.info("[TimerContextNoArgsNoResult] start in : " + startTime);
			target.run();
			long endTime = System.currentTimeMillis();
			logger.info("[TimerContextNoArgsNoResult] end in : " + endTime + ", cost : "
					+ (endTime - startTime) + " Ms");
		}

	}

	private static class TimerContextWithArgsNoResult<A> {

		private final Consumer<A> consumer;

		private TimerContextWithArgsNoResult(Consumer<A> consumer) {
			this.consumer = consumer;
		}

		void accept(A a) {
			long startTime = System.currentTimeMillis();
			logger.info("[TimerContextWithArgsNoResult] start in : " + startTime);
			consumer.accept(a);
			long endTime = System.currentTimeMillis();
			logger.info("[TimerContextWithArgsNoResult] end in : " + endTime + ", cost : "
					+ (endTime - startTime) + " Ms");
		}
	}

	private static class TimerContextNoArgsWithResult<R> {

		private final Supplier<R> supplier;

		private TimerContextNoArgsWithResult(Supplier<R> supplier) {
			this.supplier = supplier;
		}

		R acquire() {
			R result;
			long startTime = System.currentTimeMillis();
			logger.info("[TimerContextNoArgsWithResult] start in : " + startTime);
			result = supplier.get();
			long endTime = System.currentTimeMillis();
			logger.info("[TimerContextNoArgsWithResult] end in : " + endTime + ", cost : "
					+ (endTime - startTime) + " Ms");
			return result;
		}
	}

	private static class TimerContextWithArgsAndResult<A, R> {

		private final Function<A, R> function;

		private TimerContextWithArgsAndResult(Function<A, R> function) {
			this.function = function;
		}

		R apply(A a) {
			R result;
			long startTime = System.currentTimeMillis();
			logger.info("[TimerContextWithArgsAndResult] start in : " + startTime);
			result = function.apply(a);
			long endTime = System.currentTimeMillis();
			logger.info("[TimerContextWithArgsAndResult] end in : " + endTime
					+ ", cost : " + (endTime - startTime) + " Ms");
			return result;
		}
	}

	public static void invokeNoArgsNoResult(Runnable runnable) {
		TimerContextNoArgsNoResult context = new TimerContextNoArgsNoResult(runnable);
		context.run();
	}

	public static <A> void invokeWithArgsNoResult(Consumer<A> consumer, A args) {
		TimerContextWithArgsNoResult<A> context = new TimerContextWithArgsNoResult<>(
				consumer);
		context.accept(args);
	}

	public static <R> R invokeNoArgsWithResult(Supplier<R> supplier) {
		TimerContextNoArgsWithResult<R> context = new TimerContextNoArgsWithResult<>(
				supplier);
		return context.acquire();
	}

	public static <A, R> R invokeWithArgsWithResult(Function<A, R> function, A args) {
		TimerContextWithArgsAndResult<A, R> context = new TimerContextWithArgsAndResult<>(
				function);
		return context.apply(args);
	}

}
