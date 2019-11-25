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

import com.lessspring.org.executor.BaseThreadPoolExecutor;
import com.lessspring.org.executor.NameThreadFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * A simple observer pattern - the publisher
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public abstract class Publisher<T> {

	private List<Watcher> watchers = new CopyOnWriteArrayList<>();

	private BaseThreadPoolExecutor executor = new BaseThreadPoolExecutor(1, 60, TimeUnit.SECONDS, new NameThreadFactory("com.lessspring.org.config-manager.Publisher"));

	{
		executor.allowCoreThreadTimeOut(true);
	}

	public void registerWatcher(Watcher watcher) {
		watchers.add(watcher);
	}

	public void deregisterWatcher(Watcher watcher) {
		watchers.remove(watcher);
	}

	// With correction notice the Occurrence of the result, By accessing
	// CompleteableFuture to processing the Watcher

	protected void notifyAllWatcher(Occurrence<T> event) {
		executor.execute(() -> {
			for (Watcher watcher : watchers) {
				watcher.onNotify(event, this);
			}
		});
	}

	protected void notifyAllWatcher(T args) {
		final Occurrence event = Occurrence.newInstance(args);
		executor.execute(() -> {
			for (Watcher watcher : watchers) {
				watcher.onNotify(event, this);
			}
		});
	}

}
