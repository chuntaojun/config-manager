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

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.lessspring.org.ByteUtils;
import com.lessspring.org.DiskUtils;
import com.lessspring.org.PathUtils;
import com.lessspring.org.pojo.event.PublishLogEvent;
import com.lessspring.org.pojo.vo.PublishLogVO;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import org.jetbrains.annotations.NotNull;

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
	private final String title = "id,namespace,groupId,dataId,clientIp,publishTime";
	private final String layout = "%s,%s,%s,%s,%s,%s";
	private final AtomicInteger countReuseAble = new AtomicInteger(100_000);
	private FileChannel fileChannel;
	private final ScheduledExecutorService executorService = Executors
			.newSingleThreadScheduledExecutor(new ThreadFactory() {
				@Override
				public Thread newThread(@NotNull Runnable r) {
					Thread thread = new Thread(r,
							"com.lessspring.org.config-manager.tracer");
					thread.setDaemon(true);
					return thread;
				}
			});

	private static final TracerUtils SINGLE_TON = new TracerUtils();

	public static TracerUtils getSingleton() {
		return SINGLE_TON;
	}

	private Disruptor<PublishLogEvent> disruptor = DisruptorFactory
			.build(PublishLogEvent::new, "PublishLog-Disruptor");

	private TracerUtils() {
	}

	public void publishPublishEvent(PublishLogEvent source) {
		disruptor.publishEvent(
				(target, sequence) -> PublishLogEvent.copy(sequence, source, target));
	}

	@Override
	public void onEvent(PublishLogEvent event) throws Exception {
		if (countReuseAble.get() == 0) {
			countReuseAble.lazySet(100_000);
			id.getAndIncrement();
			fileChannel.close();
			fileChannel = null;
			// 自动删除老旧文件
			executorService.schedule(this::autoDeleteOldFile, 3000,
					TimeUnit.MILLISECONDS);
		}
		String fileName = tracerName + id.get() + ".csv";
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

	public List<PublishLogVO> analyzePublishLog() {
        String fileName = tracerName + id.get() + ".csv";
        File file = DiskUtils.openFile(PathUtils.finalPath(path), fileName);
        List<PublishLogVO> result = new LinkedList<>();
        Map<String, PublishLogVO> tmpRecord = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line = "";
            while((line = reader.readLine()) != null) {
                String[] infos = line.split(",");

            }
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

	private void autoDeleteOldFile() {
		int lastDeleteFileId = (int) Math.max(0, id.get() - 1);
		String fileName = tracerName + lastDeleteFileId + ".csv";
		DiskUtils.deleteFile(PathUtils.finalPath(path), fileName);
	}

}
