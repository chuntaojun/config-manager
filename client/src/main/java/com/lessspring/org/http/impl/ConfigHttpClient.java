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

import com.lessspring.org.http.HttpClient;
import com.lessspring.org.http.handler.RequestHandler;
import com.lessspring.org.http.handler.ResponseHandler;
import com.lessspring.org.http.param.Body;
import com.lessspring.org.http.param.Header;
import com.lessspring.org.http.param.HttpMethod;
import com.lessspring.org.http.param.Query;
import com.lessspring.org.model.vo.ResponseData;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static com.lessspring.org.http.param.MediaType.APPLICATION_JSON_UTF8_VALUE;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
public class ConfigHttpClient implements HttpClient {

    private OkHttpClient client;

    private final RequestHandler requestHandler = RequestHandler.getHandler();

    private final ResponseHandler responseHandler = ResponseHandler.getHandler();

    @Override
    public void init() {
        client = new OkHttpClient();
    }

    @Override
    public <T> ResponseData<T> get(String url, Header header, Query query, Class<T> cls) {
        Request request = buildRequest(buildUrl(url, query), header, Body.EMPTY, HttpMethod.GET);
        return execute(client.newCall(request), cls);
    }

    @Override
    public <T> ResponseData<T> delete(String url, Header header, Query query, Class<T> cls) {
        Request request = buildRequest(buildUrl(url, query), header, Body.EMPTY, HttpMethod.DELETE);
        return execute(client.newCall(request), cls);
    }

    @Override
    public <T> ResponseData<T> put(String url, Header header, Query query, Body body, Class<T> cls) {
        Request request = buildRequest(buildUrl(url, query), header, body, HttpMethod.PUT);
        return execute(client.newCall(request), cls);
    }

    @Override
    public <T> ResponseData<T> post(String url, Header header, Query query, Body body, Class<T> cls) {
        Request request = buildRequest(buildUrl(url, query), header, body, HttpMethod.POST);
        return execute(client.newCall(request), cls);
    }

    @Override
    public <T> void serverSendEvent(String url, Header header, Body body, Class<T> cls, EventReceiver receiver) {
        RequestBody postBody = RequestBody.create(MediaType.parse(APPLICATION_JSON_UTF8_VALUE), requestHandler.handle(body.getData()));
        Request request = new Request.Builder()
                .url(url)
                .post(postBody)
                .build();
        Call call = client.newCall(request);
        receiver.setCall(call);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                receiver.onError(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                ResponseData<T> data = ResponseData.builder()
                        .withCode(response.code())
                        .withData(responseHandler.convert(response.body().string(), cls))
                        .withErrMsg(response.message())
                        .build();
                receiver.deferEvent(data);
            }
        });
    }

    @Override
    public void destroy() {
    }

    private <T> ResponseData<T> execute(Call call, Class<T> cls) {
        ResponseData<T> data;
        try (Response response = call.execute()) {
            data = ResponseData.builder()
                    .withCode(response.code())
                    .withData(responseHandler.convert(response.body().string(), cls))
                    .withErrMsg(response.message())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    private String buildUrl(String url, Query query) {
        return url + "?" + query.toQueryUrl();
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
        Iterator<Map.Entry<String, String>> iterator = header.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }
}
