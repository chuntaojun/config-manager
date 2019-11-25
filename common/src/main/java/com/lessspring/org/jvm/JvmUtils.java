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
package com.lessspring.org.jvm;

import com.lessspring.org.Constant;
import com.sun.management.HotSpotDiagnosticMXBean;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class JvmUtils {

	/**
	 * Returns java stack traces of java threads for the current java process.
	 */
	public static List<String> jStack() throws Exception {
		final List<String> stackList = new LinkedList<>();
		final Map<Thread, StackTraceElement[]> allStackTraces = Thread
				.getAllStackTraces();
		for (final Map.Entry<Thread, StackTraceElement[]> entry : allStackTraces
				.entrySet()) {
			final Thread thread = entry.getKey();
			final StackTraceElement[] stackTraces = entry.getValue();

			stackList.add(String.format(
					"\"%s\" tid=%s isDaemon=%s priority=%s" + Constant.NEWLINE,
					thread.getName(), thread.getId(), thread.isDaemon(),
					thread.getPriority()));

			stackList.add(
					"java.lang.Thread.State: " + thread.getState() + Constant.NEWLINE);

			if (stackTraces != null) {
				for (final StackTraceElement s : stackTraces) {
					stackList.add("    " + s.toString() + Constant.NEWLINE);
				}
			}
		}
		return stackList;
	}

	/**
	 * Returns memory usage for the current java process.
	 */
	public static List<String> memoryUsage() throws Exception {
		final MemoryUsage heapMemoryUsage = MXBeanHolder.memoryMxBean
				.getHeapMemoryUsage();
		final MemoryUsage nonHeapMemoryUsage = MXBeanHolder.memoryMxBean
				.getNonHeapMemoryUsage();

		final List<String> memoryUsageList = new LinkedList<>();
		memoryUsageList.add(
				"********************************** Memory Usage **********************************"
						+ Constant.NEWLINE);
		memoryUsageList.add(
				"Heap Memory Usage: " + heapMemoryUsage.toString() + Constant.NEWLINE);
		memoryUsageList.add("NonHeap Memory Usage: " + nonHeapMemoryUsage.toString()
				+ Constant.NEWLINE);

		return memoryUsageList;
	}

	/**
	 * Returns the heap memory used for the current java process.
	 */
	public static double memoryUsed() throws Exception {
		final MemoryUsage heapMemoryUsage = MXBeanHolder.memoryMxBean
				.getHeapMemoryUsage();
		return (double) (heapMemoryUsage.getUsed()) / heapMemoryUsage.getMax();
	}

	/**
	 * Dumps the heap to the outputFile file in the same format as the hprof heap dump.
	 * @param outputFile the system-dependent filename
	 * @param live if true dump only live objects i.e. objects that are reachable from
	 *     others
	 */
	@SuppressWarnings("all")
	public static File jMap(final String outputFile, final boolean live)
			throws Exception {
		final File file = new File(outputFile);
		file.mkdirs();
		if (file.exists()) {
			file.delete();
		}
		MXBeanHolder.hotSpotDiagnosticMxBean.dumpHeap(outputFile, live);
		return file;
	}

	private static class MXBeanHolder {
		static final MemoryMXBean memoryMxBean = ManagementFactory.getMemoryMXBean();
		static final HotSpotDiagnosticMXBean hotSpotDiagnosticMxBean = ManagementFactory
				.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
	}

	private JvmUtils() {
	}

}
