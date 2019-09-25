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
package com.lessspring.org.http.impl;

import com.lessspring.org.cluster.ClusterChoose;
import com.lessspring.org.http.HttpClient;
import com.lessspring.org.http.Retry;
import com.lessspring.org.http.handler.RequestHandler;
import com.lessspring.org.http.handler.ResponseHandler;
import com.lessspring.org.http.param.Body;
import com.lessspring.org.http.param.Header;
import com.lessspring.org.http.param.HttpMethod;
import com.lessspring.org.http.param.Query;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.utils.GsonUtils;
import com.lessspring.org.utils.HttpUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSources;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.lessspring.org.http.param.MediaType.APPLICATION_JSON_UTF8_VALUE;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class ConfigHttpClient implements HttpClient {

    private OkHttpClient client;

    private final RequestHandler requestHandler = RequestHandler.getHandler();

    private final ResponseHandler responseHandler = ResponseHandler.getHandler();

    private final ClusterChoose choose;

    private AtomicReference<String> clusterIp = new AtomicReference<>();

    public ConfigHttpClient(ClusterChoose choose) {
        this.choose = choose;
    }

    @Override
    public void init() {
        client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(20_000))
                .readTimeout(Duration.ofSeconds(30_000))
                .build();
    }

    @Override
    public <T> ResponseData<T> get(String url, Header header, Query query, Class<T> cls) {
        Retry<ResponseData<T>> retry = new Retry<ResponseData<T>>() {
            @Override
            protected ResponseData<T> run() throws Exception {
                Request request = buildRequest(buildUrl(url, query), header, Body.EMPTY, HttpMethod.GET);
                return execute(client.newCall(request), cls);
            }

            @Override
            protected boolean shouldRetry(ResponseData<T> data, Throwable throwable) {
                if (!data.isOk()) {
                    return false;
                }
                refresh();
                return false;
            }

            @Override
            protected int maxRetry() {
                return 3;
            }
        };
        return retry.work();
    }

    @Override
    public <T> ResponseData<T> delete(String url, Header header, Query query, Class<T> cls) {
        Retry<ResponseData<T>> retry = new Retry<ResponseData<T>>() {
            @Override
            protected ResponseData<T> run() throws Exception {
                Request request = buildRequest(buildUrl(url, query), header, Body.EMPTY, HttpMethod.DELETE);
                return execute(client.newCall(request), cls);
            }

            @Override
            protected boolean shouldRetry(ResponseData<T> data, Throwable throwable) {
                if (!data.isOk()) {
                    return false;
                }
                refresh();
                return true;
            }

            @Override
            protected int maxRetry() {
                return 3;
            }
        };
        return retry.work();
    }

    @Override
    public <T> ResponseData<T> put(String url, Header header, Query query, Body body, Class<T> cls) {
        Retry<ResponseData<T>> retry = new Retry<ResponseData<T>>() {
            @Override
            protected ResponseData<T> run() throws Exception {
                Request request = buildRequest(buildUrl(url, query), header, body, HttpMethod.PUT);
                return execute(client.newCall(request), cls);
            }

            @Override
            protected boolean shouldRetry(ResponseData<T> data, Throwable throwable) {
                if (!data.isOk()) {
                    return false;
                }
                refresh();
                return true;
            }

            @Override
            protected int maxRetry() {
                return 3;
            }
        };
        return retry.work();

    }

    @Override
    public <T> ResponseData<T> post(String url, Header header, Query query, Body body, Class<T> cls) {
        Retry<ResponseData<T>> retry = new Retry<ResponseData<T>>() {
            @Override
            protected ResponseData<T> run() throws Exception {
                Request request = buildRequest(buildUrl(url, query), header, body, HttpMethod.POST);
                return execute(client.newCall(request), cls);
            }

            @Override
            protected boolean shouldRetry(ResponseData<T> data, Throwable throwable) {
                if (!data.isOk()) {
                    return false;
                }
                refresh();
                return true;
            }

            @Override
            protected int maxRetry() {
                return 3;
            }
        };
        return retry.work();
    }

    @Override
    public <T> void serverSendEvent(String url, Header header, Body body, Class<T> cls, EventReceiver receiver) {
        Retry<Void> retry = new Retry<Void>() {
            @Override
            protected Void run() throws Exception {
                RequestBody postBody = RequestBody.create(MediaType.parse(APPLICATION_JSON_UTF8_VALUE), requestHandler.handle(body.getData()));
                Request.Builder builder = new Request.Builder()
                        .url(buildUrl(url))
                        .post(postBody);
                initHeader(header, builder);
                EventSource.Factory factory = EventSources.createFactory(client);
                EventSource source = factory.newEventSource(builder.build(), new ServerSentEventListener<T>(receiver, cls));
                receiver.setEventSource(source);
                return null;
            }

            @Override
            protected boolean shouldRetry(Void data, Throwable throwable) {
                return false;
            }

            @Override
            protected int maxRetry() {
                return 3;
            }
        };

        retry.work();
    }

    @Override
    public void destroy() {
        ServerSentEventListener.clean();
    }

    private <T> ResponseData<T> execute(Call call, Class<T> cls) throws IOException {
        ResponseData<T> data;
        Response response = call.execute();
        data = ResponseData.builder()
                .withCode(response.code())
                .withData(responseHandler.convert(response.body().string(), cls))
                .withErrMsg(response.message())
                .build();
        return data;
    }

    private String buildUrl(String url) {
        return buildUrl(url, Query.EMPTY);
    }

    private String buildUrl(String url, Query query) {
        String queryStr = "";
        if (!query.isEmpty()) {
            queryStr = "?" + query.toQueryUrl();
        }
        if (url.startsWith("/")) {
            return HttpUtils.buildBasePath(getServerIp(), url + queryStr);
        }
        return HttpUtils.buildBasePath(getServerIp(), "/" + url + queryStr);
    }

    private Request buildRequest(String url, Header header, Body body, HttpMethod method) {
        Request.Builder builder = new Request.Builder();
        builder = builder.url(url);
        if (method == HttpMethod.GET) {
            builder = builder.get();
        } else if (method == HttpMethod.DELETE) {
            builder = builder.delete();
        } else if (method == HttpMethod.PUT) {
            RequestBody putBody = RequestBody.create(MediaType.parse(APPLICATION_JSON_UTF8_VALUE), requestHandler.handle(body.getData()));
            builder = builder.put(putBody);
        } else if (method == HttpMethod.POST) {
            RequestBody postBody = RequestBody.create(MediaType.parse(APPLICATION_JSON_UTF8_VALUE), requestHandler.handle(body.getData()));
            builder = builder.post(postBody);
        } else {
            throw new IllegalArgumentException("Does not support HTTP request type");
        }
        initHeader(header, builder);
        return builder.build();
    }

    private void initHeader(final Header header, Request.Builder builder) {
        Iterator<Map.Entry<String, String>> iterator = header.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            builder.addHeader(entry.getKey(), entry.getValue());
        }
    }

    private String getServerIp() {
        String ip = clusterIp.get();
        if (StringUtils.isEmpty(ip)) {
            clusterIp.set(choose.getLastClusterIp());
        }
        return clusterIp.get();
    }

    private void refresh() {
        clusterIp.set("");
    }
}
