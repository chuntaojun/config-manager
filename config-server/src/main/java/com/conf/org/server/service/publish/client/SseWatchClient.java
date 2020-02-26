package com.conf.org.server.service.publish.client;

import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.FluxSink;

import java.util.Map;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019/12/13 2:52 下午
 */
public class SseWatchClient extends WatchClient {

	private FluxSink sink;

	public static SseWatchClientBuilder builder() {
		return new SseWatchClientBuilder();
	}

	public FluxSink getSink() {
		return sink;
	}

	public void setSink(FluxSink sink) {
		this.sink = sink;
	}

	@Override
	public void onChangeMd5(String key, String lastMd5) {
		checkKey.put(key, lastMd5);
	}

	public static final class SseWatchClientBuilder {
		protected String clientId;
		private FluxSink sink;
		private String clientIp;
		private String namespaceId;
		private Map<String, String> checkKey;
		private ServerHttpResponse response;

		private SseWatchClientBuilder() {
		}

		public SseWatchClientBuilder clientId(String clientId) {
			this.clientId = clientId;
			return this;
		}

		public SseWatchClientBuilder sink(FluxSink sink) {
			this.sink = sink;
			return this;
		}

		public SseWatchClientBuilder clientIp(String clientIp) {
			this.clientIp = clientIp;
			return this;
		}

		public SseWatchClientBuilder namespaceId(String namespaceId) {
			this.namespaceId = namespaceId;
			return this;
		}

		public SseWatchClientBuilder checkKey(Map<String, String> checkKey) {
			this.checkKey = checkKey;
			return this;
		}

		public SseWatchClientBuilder response(ServerHttpResponse response) {
			this.response = response;
			return this;
		}

		public SseWatchClient build() {
			SseWatchClient sseWatchClient = new SseWatchClient();
			sseWatchClient.setSink(sink);
			sseWatchClient.setClientId(clientId);
			sseWatchClient.response = this.response;
			sseWatchClient.clientIp = this.clientIp;
			sseWatchClient.namespaceId = this.namespaceId;
			sseWatchClient.checkKey = this.checkKey;
			return sseWatchClient;
		}
	}
}
