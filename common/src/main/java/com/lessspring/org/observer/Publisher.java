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

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * A simple observer pattern - the publisher
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public abstract class Publisher<T> {

	private List<Watcher<T>> watchers = new CopyOnWriteArrayList<>();

	private FluxSink<Occurrence<T>> sink;

	public Publisher() {
		Flux.create((Consumer<FluxSink<Occurrence<T>>>) tFluxSink -> sink = tFluxSink)
				.subscribe(tOccurrence -> {
					watchers.parallelStream().forEach(
							watcher -> watcher.onNotify(tOccurrence, Publisher.this));
				});
	}

	public void registerWatcher(Watcher<T> watcher) {
		watchers.add(watcher);
	}

	public void deregisterWatcher(Watcher watcher) {
		watchers.remove(watcher);
	}

	// With correction notice the Occurrence of the result, By accessing
	// CompletableFuture to processing the Watcher

	protected void notifyAllWatcher(Occurrence<T> event) {
		sink.next(event);
	}

	protected void notifyAllWatcher(T args) {
		notifyAllWatcher(Occurrence.newInstance(args));
	}

}
