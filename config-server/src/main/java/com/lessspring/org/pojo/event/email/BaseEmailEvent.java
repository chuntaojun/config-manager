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
package com.lessspring.org.pojo.event.email;

import java.io.File;
import java.util.Objects;

import com.lessspring.org.utils.PropertiesEnum;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public abstract class BaseEmailEvent {

	private PropertiesEnum.EmailType type;
	private String eventLabel;
	private String title;
	private String msg;
	private File attachment;

	public BaseEmailEvent(PropertiesEnum.EmailType type) {
		this.type = type;
		this.eventLabel = type.getDesc();
	}

	public PropertiesEnum.EmailType getType() {
		return type;
	}

	public void setType(PropertiesEnum.EmailType type) {
		this.type = type;
	}

	public String getEventLabel() {
		return eventLabel;
	}

	public void setEventLabel(String eventLabel) {
		this.eventLabel = eventLabel;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public File getAttachment() {
		return attachment;
	}

	public void setAttachment(File attachment) {
		this.attachment = attachment;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public boolean hasAttachment() {
		return Objects.nonNull(attachment) && attachment.exists();
	}
}
