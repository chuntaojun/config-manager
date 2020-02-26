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
package com.conf.org.executor;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import com.conf.org.PathUtils;
import com.conf.org.utils.ByteUtils;
import com.conf.org.jvm.JvmUtils;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public abstract class BaseRejectedExecutionHandler implements RejectedExecutionHandler {

	private static Logger logger = Logger.getAnonymousLogger();
	private static final String logCacheDir = "jvm-log";

	private final String threadPoolName;
	private final AtomicBoolean dumpNeeded;
	private final String dumpPrefixName;

	protected BaseRejectedExecutionHandler(String threadPoolName, boolean dumpNeeded,
			String dumpPrefixName) {
		this.threadPoolName = threadPoolName;
		this.dumpNeeded = new AtomicBoolean(dumpNeeded);
		this.dumpPrefixName = dumpPrefixName;
	}

	protected void dumpJvmInfoIfNeeded() {
		if (this.dumpNeeded.getAndSet(false)) {
			final String now = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
					.format(new Date());
			final String name = this.threadPoolName + "_" + now;
			try (final FileOutputStream fileOutput = new FileOutputStream(
					new File(PathUtils.finalPath(logCacheDir),
							this.dumpPrefixName + "_dump_" + name + ".log"))) {

				final List<String> stacks = JvmUtils.jStack();
				for (final String s : stacks) {
					fileOutput.write(ByteUtils.toBytes(s));
				}

				final List<String> memoryUsages = JvmUtils.memoryUsage();
				for (final String m : memoryUsages) {
					fileOutput.write(ByteUtils.toBytes(m));
				}

				if (JvmUtils.memoryUsed() > 0.9) {
					JvmUtils.jMap(this.dumpPrefixName + "_dump_" + name + ".bin", false);
				}
			}
			catch (final Throwable t) {
				logger.throwing("Dump jvm info error: {}.", "dumpJvmInfoIfNeeded", t);
			}
		}
	}
}
