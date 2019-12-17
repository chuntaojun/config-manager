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

package com.lessspring.org.server.web;

import com.lessspring.org.server.configuration.http.ConfVisitor;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RequestPredicate;
import reactor.util.function.Tuple2;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019/12/16 7:48 下午
 */
@SuppressWarnings("all")
public abstract class BaseRouter {

    protected void registerVisitor(Tuple2<RequestPredicate, HandlerFunction>... tuple2s) {
        for (Tuple2<RequestPredicate, HandlerFunction> tuple2 : tuple2s) {
            ConfVisitor confVisitor = new ConfVisitor();
            confVisitor.setHandlerFunction(tuple2.getT2());
            tuple2.getT1().accept(confVisitor);
        }
    }

}
