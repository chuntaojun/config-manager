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
package com.lessspring.org.http;

import com.lessspring.org.LifeCycle;
import com.lessspring.org.http.impl.EventReceiver;
import com.lessspring.org.http.param.Body;
import com.lessspring.org.http.param.Header;
import com.lessspring.org.http.param.Query;
import com.lessspring.org.model.vo.ResponseData;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public interface HttpClient extends LifeCycle {

    /**
     * http get
     *
     * @param url url
     * @param header http header param
     * @param query http query param
     * @param cls return type
     * @return {@link ResponseData<T>}
     */
    <T> ResponseData<T> get(String url, Header header, Query query, Class<T> cls);

    /**
     * http delete
     *
     * @param url url
     * @param header http header param
     * @param query http query param
     * @param cls return type
     * @return {@link ResponseData<T>}
     */
    <T> ResponseData<T> delete(String url, Header header, Query query, Class<T> cls);

    /**
     * http put
     *
     * @param url url
     * @param header http header param
     * @param query http query param
     * @param body http body param
     * @param cls return type
     * @return {@link ResponseData}
     */
    <T> ResponseData<T> put(String url, Header header, Query query, Body body, Class<T> cls);

    /**
     * http post
     *
     * @param url url
     * @param header http header param
     * @param query http query param
     * @param body http body param
     * @param cls return type
     * @return {@link ResponseData}
     */
    <T> ResponseData<T> post(String url, Header header, Query query, Body body, Class<T> cls);

    /**
     * server send event
     *
     * @param url url
     * @param header http header param
     * @param query http query param
     * @param cls return type
     * @param receiver {@link EventReceiver}
     */
    <T> void serverSendEvent(String url, Header header, Query query, Class<T> cls, EventReceiver receiver);

}
