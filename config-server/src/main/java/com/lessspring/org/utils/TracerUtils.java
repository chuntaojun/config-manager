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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.lessspring.org.DiskUtils;
import com.lessspring.org.PathUtils;
import com.lessspring.org.executor.NameThreadFactory;
import com.lessspring.org.pojo.event.config.PublishLogEvent;
import com.lessspring.org.pojo.event.config.PublishLogEventHandler;
import com.lessspring.org.pojo.vo.PublishLogVO;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.stereotype.Component;

/**
 * Notify the tracker
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Component
public final class TracerUtils implements WorkHandler<PublishLogEventHandler> {

	private long id = 0;
	private final String tracerName = "config-manager-tracer-";
	private final String path = "watch-publish-tracer";
	private int countReuseAble = 100_000;
	private FileChannel fileChannel;
	private final ScheduledExecutorService executorService = Executors
			.newSingleThreadScheduledExecutor(
					new NameThreadFactory("com.lessspring.org.config-manager.tracer-"));

	private static final TracerUtils SINGLE_TON = new TracerUtils();

	public static TracerUtils getSingleton() {
		return SINGLE_TON;
	}

	private Disruptor<PublishLogEventHandler> disruptor = DisruptorFactory
			.build(PublishLogEventHandler::new, PublishLogEvent.class);

	private TracerUtils() {
		// 自动删除老旧文件
		executorService.scheduleWithFixedDelay(this::autoDeleteOldFile, 6, 12,
				TimeUnit.HOURS);
	}

	public void publishPublishEvent(PublishLogEvent source) {
		disruptor.publishEvent((target, sequence) -> {
			source.setSequence(sequence);
			target.rest();
			target.setEvent(source);
		});
		disruptor.handleEventsWithWorkerPool(this);
	}

	@Override
	public void onEvent(PublishLogEventHandler eventHandler) throws Exception {
		final PublishLogEvent event = eventHandler.getEvent();
		if (countReuseAble == 0) {
			countReuseAble = 100_000;
			id++;
			fileChannel.close();
			fileChannel = null;
		}
		String fileName = tracerName + id + ".csv";
		if (Objects.isNull(fileChannel)) {
			fileChannel = new FileInputStream(
					DiskUtils.openFile(PathUtils.finalPath(path), fileName)).getChannel();
			String title = "id,namespace,groupId,dataId,clientIp,publishTime";
			fileChannel.write(ByteBuffer.wrap(ByteUtils.toBytes(title)));
		}
		String layout = "%s,%s,%s,%s,%s,%s";
		final String logRecord = String.format(layout, event.getSequence(),
				event.getNamespaceId(), event.getGroupId(), event.getDataId(),
				event.getClientIp(), event.getPublishTime());
		fileChannel.write(ByteBuffer.wrap(ByteUtils.toBytes(logRecord)));
		countReuseAble--;
	}

	public Map<String, PublishLogVO> analyzePublishLog() {
		String fileName = tracerName + id + ".csv";
		File file = DiskUtils.openFile(PathUtils.finalPath(path), fileName);
		Map<String, PublishLogVO> tmpRecord = new LinkedHashMap<>(32);
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file)))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] infos = line.split(",");
				RequireHelper.requireNotNull(infos, "This line is null");
				RequireHelper.requireEquals(infos.length, 5,
						"This publish record is illegal");
				final String clientIp = infos[4];
				tmpRecord.computeIfAbsent(clientIp, s -> new PublishLogVO());
				PublishLogVO logVO = tmpRecord.get(clientIp);
				Map<String, String> attachment = new LinkedHashMap<>();
				attachment.put("namespaceId", infos[1]);
				attachment.put("groupId", infos[2]);
				attachment.put("dataId", infos[3]);
				attachment.put("time", infos[5]);
				logVO.addRecord(attachment);
			}
			return tmpRecord;
		}
		catch (IOException e) {
			return Collections.emptyMap();
		}
	}

	private void autoDeleteOldFile() {
		File file = new File(PathUtils.finalPath(path));
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			assert files != null;
			Arrays.sort(files, (o1, o2) -> (int) (o1.lastModified() - o2.lastModified()));
			for (int i = 0; i < files.length - 10; i++) {
				files[i].delete();
			}
		}
	}

}
