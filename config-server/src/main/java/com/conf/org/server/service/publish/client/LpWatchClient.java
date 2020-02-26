package com.conf.org.server.service.publish.client;

import java.util.Map;

import reactor.core.publisher.MonoSink;

import org.springframework.http.server.reactive.ServerHttpResponse;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019/12/13 2:53 下午
 */
public class LpWatchClient extends WatchClient {

	private long holdTime;

	private boolean active = true;

	private MonoSink monoSink;

	public static LpWatchClientBuilder builder() {
		return new LpWatchClientBuilder();
	}

	public long getHoldTime() {
		return holdTime;
	}

	public void setHoldTime(long holdTime) {
		this.holdTime = holdTime;
	}

	public void shutdown() {
		active = false;
	}

	public boolean isActive() {
		return active;
	}

	public void send(Object data) {
		monoSink.success(data);
	}

	public void setMonoSink(MonoSink<?> monoSink) {
		this.monoSink = monoSink;
	}

	@Override
	public void onChangeMd5(String key, String lastMd5) {

	}

	public static final class LpWatchClientBuilder {
		protected String clientId;
		protected String clientIp;
		protected String namespaceId;
		protected Map<String, String> checkKey;
		protected ServerHttpResponse response;
		private long holdTime;
		private MonoSink<?> monoSink;

		private LpWatchClientBuilder() {
		}

		public LpWatchClientBuilder clientId(String clientId) {
			this.clientId = clientId;
			return this;
		}

		public LpWatchClientBuilder holdTime(long holdTime) {
			this.holdTime = holdTime;
			return this;
		}

		public LpWatchClientBuilder clientIp(String clientIp) {
			this.clientIp = clientIp;
			return this;
		}

		public LpWatchClientBuilder namespaceId(String namespaceId) {
			this.namespaceId = namespaceId;
			return this;
		}

		public LpWatchClientBuilder checkKey(Map<String, String> checkKey) {
			this.checkKey = checkKey;
			return this;
		}

		public LpWatchClientBuilder response(ServerHttpResponse response) {
			this.response = response;
			return this;
		}

		public LpWatchClientBuilder monoSink(MonoSink monoSink) {
			this.monoSink = monoSink;
			return this;
		}

		public LpWatchClient build() {
			LpWatchClient lpWatchClient = new LpWatchClient();
			lpWatchClient.setClientId(clientId);
			lpWatchClient.setHoldTime(holdTime);
			lpWatchClient.response = this.response;
			lpWatchClient.clientIp = this.clientIp;
			lpWatchClient.namespaceId = this.namespaceId;
			lpWatchClient.checkKey = this.checkKey;
			lpWatchClient.monoSink = this.monoSink;
			return lpWatchClient;
		}
	}
}
