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
package com.lessspring.org.executor;

import java.util.logging.Logger;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class WrapperRunnable extends TimeTask implements Runnable {

	private static final Logger logger = Logger.getAnonymousLogger();

	private final Runnable target;

	public WrapperRunnable(Runnable target) {
		this.target = target;
	}

	@Override
	public void run() {
		// logger.info(Thread.currentThread().getName() + " start work");
		// start();
		target.run();
		// logger.info(Thread.currentThread().getName() + " end work, spend time : "
		// + spendTime());
	}

}
