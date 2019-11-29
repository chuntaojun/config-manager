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

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019-11-23 14:33
 */
public final class ThreadUtils {

	public static void sleep(long time) {
		try {
			Thread.sleep(time);
		}
		catch (InterruptedException ignore) {

		}
	}

	public static void onWait(Object o) {
		synchronized (o) {
			try {
				o.wait();
			}
			catch (InterruptedException ignore) {

			}
		}
	}

	public static void onWait(Object o, long timeoutMillis) {
		synchronized (o) {
			try {
				o.wait(timeoutMillis);
			}
			catch (InterruptedException ignore) {

			}
		}
	}

	public static void onNotify(Object o) {
		synchronized (o) {
			o.notify();
		}
	}

}
