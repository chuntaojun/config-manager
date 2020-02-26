package com.conf.org.raft;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.conf.org.DiskUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

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
@Slf4j
public class ClusterServerTest {

	@Test
	public void testReadFile() {
		try (InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("cluster.properties")) {
			File file = new File(Thread.currentThread().getContextClassLoader()
					.getResource("cluster.properties").getPath());
			while (true) {
				System.out.println(DiskUtils.readFile(file));
				Thread.sleep(1000);
			}
		}
		catch (IOException | InterruptedException e) {
			log.error("Server");
			throw new RuntimeException(e);
		}
	}

}