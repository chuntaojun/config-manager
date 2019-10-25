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
package com.lessspring.org.utils;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.lessspring.org.ByteUtils;
import com.lessspring.org.DiskUtils;
import com.lessspring.org.PathUtils;
import com.lessspring.org.pojo.event.PublishLogEvent;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;

/**
 * Notify the tracker
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class TracerUtils implements WorkHandler<PublishLogEvent> {

	private final AtomicLong id = new AtomicLong(0);
	private final String tracerName = "config-manager-tracer-";
	private final String path = "watch-publish-tracer";
	private final String title = "id|namespace|groupId|dataId|clientIp|publishTime";
	private final String layout = "%s|%s|%s|%s|%s|%s";
	private final AtomicInteger countReuseAble = new AtomicInteger(10_000);
	private FileChannel fileChannel;

	private static final TracerUtils SINGLE_TON = new TracerUtils();

	public static TracerUtils getSingleton() {
		return SINGLE_TON;
	}

	private Disruptor<PublishLogEvent> disruptor = DisruptorFactory
			.build(PublishLogEvent::new, "com.lessspring.org.config-manager.publishLog");

	private TracerUtils() {
	}

	public void publishPublishEvent(PublishLogEvent source) {
		disruptor.publishEvent(
				(target, sequence) -> PublishLogEvent.copy(sequence, source, target));
	}

	@Override
	public void onEvent(PublishLogEvent event) throws Exception {
		if (countReuseAble.get() == 0) {
			id.getAndIncrement();
			fileChannel.close();
			fileChannel = null;
		}
		String fileName = tracerName + id.get();
		if (Objects.isNull(fileChannel)) {
			fileChannel = new FileInputStream(
					DiskUtils.openFile(PathUtils.finalPath(path), fileName)).getChannel();
			fileChannel.write(ByteBuffer.wrap(ByteUtils.toBytes(title)));
		}
		final String logRecord = String.format(layout, event.getSequence(),
				event.getNamespaceId(), event.getGroupId(), event.getDataId(),
				event.getClientIp(), event.getPublishTime());
		fileChannel.write(ByteBuffer.wrap(ByteUtils.toBytes(logRecord)));
		countReuseAble.decrementAndGet();
	}

}
