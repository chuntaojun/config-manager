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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class LoggerHolder {

    /**
     * 默认的日志
     */
    public static  final Logger defaultLog = LoggerFactory.getLogger("com.alibaba.nacos.config.startLog");

    /**
     * 致命错误，需要告警
     */
    public static final Logger fatalLog = LoggerFactory
            .getLogger("com.alibaba.nacos.config.fatal");

    /**
     * 客户端GET方法获取数据的日志
     */
    public static final Logger pullLog = LoggerFactory
            .getLogger("com.alibaba.nacos.config.pullLog");

    public static final Logger pullCheckLog = LoggerFactory
            .getLogger("com.alibaba.nacos.config.pullCheckLog");
    /**
     * 从DB dump数据的日志
     */
    public static final Logger dumpLog = LoggerFactory
            .getLogger("com.alibaba.nacos.config.dumpLog");

    public static final Logger memoryLog = LoggerFactory
            .getLogger("com.alibaba.nacos.config.monitorLog");

    public static final Logger clientLog = LoggerFactory
            .getLogger("com.alibaba.nacos.config.clientLog");

    public static final Logger traceLog = LoggerFactory
            .getLogger("com.alibaba.nacos.config.traceLog");

    public static final Logger notifyLog = LoggerFactory
            .getLogger("com.alibaba.nacos.config.notifyLog");

    public static void setLogLevel(String logName, String level) {

        switch (logName) {
            case "config-server":
                ((org.slf4j.Logger) defaultLog).(Level.valueOf(level));
                break;
            case "config-fatal":
                ((org.slf4j.Logger) fatalLog).setLevel(Level.valueOf(level));
                break;
            case "config-pull":
                ((org.slf4j.Logger) pullLog).setLevel(Level.valueOf(level));
                break;
            case "config-pull-check":
                ((org.slf4j.Logger) pullCheckLog).setLevel(Level.valueOf(level));
                break;
            case "config-dump":
                ((org.slf4j.Logger) dumpLog).setLevel(Level.valueOf(level));
                break;
            case "config-memory":
                ((org.slf4j.Logger) memoryLog).setLevel(Level.valueOf(level));
                break;
            case "config-client-request":
                ((org.slf4j.Logger) clientLog).setLevel(Level.valueOf(level));
                break;
            case "config-trace":
                ((org.slf4j.Logger) traceLog).setLevel(Level.valueOf(level));
                break;
            case "config-notify":
                ((org.slf4j.Logger) notifyLog).setLevel(Level.valueOf(level));
                break;
            default:
                break;
        }

    }

}
