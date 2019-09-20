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
package com.lessspring.org;

import com.lessspring.org.config.ConfigService;
import com.lessspring.org.model.dto.ConfigInfo;
import org.junit.Test;

import java.util.Scanner;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class Test_ClusterInfo {

    @Test
    public void testSplitClusterInfo() {
        String clusterInfo = "127.0.0.1:2959";
        Configuration configuration = new Configuration();
        configuration.setServers(clusterInfo);
        ConfigService configService = new ClientConfigService(configuration);
        configService.addListener("DEFAULT_GROUP", "TEST", new AbstractListener() {
            @Override
            public void onReceive(ConfigInfo configInfo) {
                System.out.println(configInfo.toString());
            }
        });
        Scanner scanner = new Scanner(System.in);
        scanner.next();
    }

}
