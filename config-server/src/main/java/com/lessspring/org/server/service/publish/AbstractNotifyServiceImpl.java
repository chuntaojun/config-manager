package com.lessspring.org.server.service.publish;

import com.lessspring.org.NameUtils;
import com.lessspring.org.StrackTracekUtils;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.server.pojo.CacheItem;
import com.lessspring.org.server.pojo.ReadWork;
import com.lessspring.org.server.pojo.event.config.NotifyEvent;
import com.lessspring.org.server.pojo.event.config.NotifyEventHandler;
import com.lessspring.org.server.pojo.event.config.PublishLogEvent;
import com.lessspring.org.server.service.config.ConfigCacheItemManager;
import com.lessspring.org.server.service.publish.client.WatchClient;
import com.lessspring.org.server.utils.GsonUtils;
import com.lessspring.org.server.utils.SystemEnv;
import com.lmax.disruptor.WorkHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019/12/13 2:50 下午
 */
@Slf4j
public abstract class AbstractNotifyServiceImpl
		implements NotifyService, WorkHandler<NotifyEventHandler> {

	protected final SystemEnv systemEnv = SystemEnv.getSingleton();
	final Object monitor = new Object();
	long clientCnt = 0;
	Map<String, Map<String, Set<WatchClient>>> watchClientManager = new ConcurrentHashMap<>(
			8);

	@Autowired
	@Lazy
	protected TraceAnalyzer tracer;

	@Autowired
	@Lazy
	protected ConfigCacheItemManager cacheItemManager;

	protected void createWatchClient(WatchClient watchClient) {
		Map<String, String> listenKeys = watchClient.getCheckKey();
		// According to the monitoring configuration key, registered to a
		// different key corresponding to the listener list
		listenKeys.forEach((key, value) -> {
			Map<String, Set<WatchClient>> clientsMap = watchClientManager.computeIfAbsent(
					watchClient.getNamespaceId(), s -> new ConcurrentHashMap<>(4));
			clientsMap.computeIfAbsent(key, s -> new CopyOnWriteArraySet<>());
			clientsMap.get(key).add(watchClient);
		});
		synchronized (monitor) {
			clientCnt++;
			log.info("【WatchClientManager】now watchClient count is : {}", clientCnt);
		}
		// A quick comparison, listens for client direct access to the latest
		// configuration when registering for the first time
		doQuickCompare(watchClient);
	}

	@Override
	public void doQuickCompare(WatchClient watchClient) {
		Map<String, String> checkMd5 = watchClient.getCheckKey();
		// groupId@#@dataId => md5Sign
		checkMd5.forEach((key, value) -> {
			String[] info = NameUtils.splitName(key);
			final CacheItem cacheItem = cacheItemManager
					.queryCacheItem(watchClient.getNamespaceId(), info[0], info[1]);
			if (!compareConfigSign(value, cacheItem.getLastMd5())) {
				return;
			}
			final ReadWork readWork = new ReadWork() {
				@Override
				public void job() {
					String content = cacheItemManager
							.readCacheFromDisk(watchClient.getNamespaceId(), key);
					if (StringUtils.isEmpty(content)) {
						// To conduct a read operation, will update CacheItem
						// information
						ConfigInfo configInfo = cacheItemManager.loadConfigFromDB(
								watchClient.getNamespaceId(), info[0], info[1]);
						if (configInfo == null) {
							return;
						}
						content = GsonUtils.toJson(configInfo);
					}
					if (cacheItem.canRead(watchClient.getClientIp())) {
						tracer.publishPublishEvent(PublishLogEvent.builder()
								.clientIp(watchClient.getClientIp())
								.namespaceId(watchClient.getNamespaceId())
								.groupId(info[0]).dataId(info[1])
								.publishTime(System.currentTimeMillis()).build());
						writeResponse(watchClient, GsonUtils.toJson(content));
					}
				}

				@Override
				public void onError(Exception exception) {
					log.error(
							"[doQuickCompare] when execute read job has some error : {}",
							StrackTracekUtils.stackTrace(exception));
				}
			};
			cacheItem.executeReadWork(readWork);
		});
	}

	// Use the event framework, receiving NotifyEvent events, the
	// configuration changes on delivery to the client

	// TODO Push the trajectory logging

	@Override
	public void onEvent(NotifyEventHandler eventHandler) throws Exception {
		NotifyEvent event = eventHandler.getEvent();
		final CacheItem cacheItem = cacheItemManager.queryCacheItem(
				event.getNamespaceId(), event.getGroupId(), event.getDataId());
		final String configInfoJson = cacheItemManager.readCacheFromDisk(
				event.getNamespaceId(), event.getGroupId(), event.getDataId(),
				cacheItem.isBeta());
		long[] finishWorks = new long[1];
		final String key = NameUtils.buildName(event.getGroupId(), event.getDataId());
		Set<Map.Entry<String, Set<WatchClient>>> set = watchClientManager
				.getOrDefault(event.getNamespaceId(), Collections.emptyMap()).entrySet();
		Stream<Map.Entry<String, Set<WatchClient>>> stream;
		long parallelThreshold = 100;
		if (clientCnt >= parallelThreshold) {
			stream = set.parallelStream();
		}
		else {
			stream = set.stream();
		}
		stream.filter(entry -> Objects.equals(key, entry.getKey()))
				.flatMap(stringSetEntry -> stringSetEntry.getValue().stream())
				.forEach(client -> {
					// If it is beta configuration file, you need to check the
					// client IP information
					if (event.isBeta()) {
						if (cacheItem.canRead(client.getClientIp())) {
							return;
						}
					}
					//
					if (!compareConfigSign(cacheItem.getLastMd5(),
							client.getCheckKey().get(key))) {
						return;
					}
					try {
						writeResponse(client, configInfoJson);
						finishWorks[0]++;
						tracer.publishPublishEvent(PublishLogEvent.builder()
								.namespaceId(event.getNamespaceId())
								.groupId(event.getGroupId()).dataId(event.getDataId())
								.clientIp(client.getClientIp())
								.publishTime(System.currentTimeMillis()).build());
					}
					catch (Exception e) {
						log.error("[Notify WatchClient has Error] : {}", e.getMessage());
					}
				});
		log.info("total notify clients finish success is : {}", finishWorks[0]);
	}

	/**
	 * 将数据响应给客户端
	 *
	 * @param client {@link WatchClient}
	 * @param data data
	 */
	protected abstract void writeResponse(WatchClient client, Object data);

	/**
	 * 比较配置签名是否发生变更
	 *
	 * @param oldSign client端的配置签名
	 * @param newSign server端的配置签名
	 * @return 比较结果
	 */
	protected abstract boolean compareConfigSign(String oldSign, String newSign);
}
