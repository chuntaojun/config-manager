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

package com.lessspring.org.server.service.common;

import java.util.ArrayList;
import java.util.List;

import com.lessspring.org.server.utils.PropertiesEnum;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019-11-25 08:57
 */
@ConfigurationProperties(prefix = "com.lessspring.org.config-manager.email.notify")
public class EmailNotifyProperties {

	private List<NotifyReceiver> notifyReceivers = new ArrayList<>();

	public List<NotifyReceiver> getNotifyReceivers() {
		return notifyReceivers;
	}

	public void setNotifyReceivers(List<NotifyReceiver> notifyReceivers) {
		this.notifyReceivers = notifyReceivers;
	}

	public static class NotifyReceiver {

		private PropertiesEnum.EmailType notifyType;
		private String receiverEmail;

		public NotifyReceiver() {
		}

		public NotifyReceiver(PropertiesEnum.EmailType notifyType, String receiverEmail) {
			this.notifyType = notifyType;
			this.receiverEmail = receiverEmail;
		}

		public PropertiesEnum.EmailType getNotifyType() {
			return notifyType;
		}

		public void setNotifyType(PropertiesEnum.EmailType notifyType) {
			this.notifyType = notifyType;
		}

		public String getReceiverEmail() {
			return receiverEmail;
		}

		public void setReceiverEmail(String receiverEmail) {
			this.receiverEmail = receiverEmail;
		}
	}
}
