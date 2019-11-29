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
package com.lessspring.org.observer;

import java.lang.ref.SoftReference;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Publishers of events, By accessing CompleteableFuture to processing the Watcher
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class Occurrence<T> {

	private SoftReference<T> origin;
	private CompletableFuture<Boolean> answer;

	private Occurrence() {
	}

	public static <T> Occurrence<T> newInstance(T event) {
		Occurrence<T> occurrence = new Occurrence<>();
		occurrence.origin = new SoftReference<>(event);
		return occurrence;
	}

	public static <T> Occurrence<T> newInstanceWithFuture(T args) {
		Occurrence<T> occurrence = newInstance(args);
		occurrence.answer = new CompletableFuture<>();
		return occurrence;
	}

	public T getOrigin() {
		return origin.get();
	}

	public Optional<CompletableFuture<Boolean>> getAnswer() {
		return Optional.ofNullable(answer);
	}
}
