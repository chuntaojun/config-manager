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
package com.conf.org.server.service.common.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.conf.org.observer.Occurrence;
import com.conf.org.observer.Publisher;
import com.conf.org.observer.Watcher;
import com.conf.org.server.service.common.EmailService;
import com.conf.org.server.pojo.event.email.BaseEmailEvent;
import com.conf.org.server.service.common.EmailNotifyProperties;
import com.conf.org.server.utils.PropertiesEnum;
import com.conf.org.server.utils.SystemEnv;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component
public class EmailServiceImpl extends Publisher<BaseEmailEvent>
		implements EmailService, Watcher<BaseEmailEvent> {

	private final JavaMailSender mailSender;
	@Autowired
	private EmailNotifyProperties emailNotifyProperties;
	private Map<PropertiesEnum.EmailType, List<String>> receivers = new HashMap<>();
	private SystemEnv systemEnv;

	public EmailServiceImpl(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	@PostConstruct
	public void init() {
		registerWatcher(this);
		systemEnv = SystemEnv.getSingleton();
		for (EmailNotifyProperties.NotifyReceiver receiver : emailNotifyProperties
				.getNotifyReceivers()) {
			receivers.computeIfAbsent(receiver.getNotifyType(),
					emailType -> new ArrayList<>());
			List<String> list = receivers.get(receiver.getNotifyType());
			list.add(receiver.getReceiverEmail());
		}
	}

	@Override
	public void publishEmailEvent(BaseEmailEvent baseEmailEvent) {
		notifyAllWatcher(baseEmailEvent);
	}

	private void sendEmail(String toAddr, String title, String content) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(systemEnv.getEmailHost());
		message.setTo(toAddr);
		message.setSubject(title);
		message.setText(content);
		try {
			mailSender.send(message);
			log.info("email has already send");
		}
		catch (Exception e) {
			log.error("send email has some error : {}", e);
		}
	}

	private void sendAttachmentsMail(String toAddr, String title, String content,
			File file) {
		MimeMessage message = mailSender.createMimeMessage();

		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(systemEnv.getEmailHost());
			helper.setTo(toAddr);
			helper.setSubject(title);
			helper.setText(content, true);
			FileSystemResource attachment = new FileSystemResource(file);
			helper.addAttachment(file.getName(), attachment);
			mailSender.send(message);
			file.delete();
		}
		catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onNotify(Occurrence<BaseEmailEvent> occurrence, Publisher publisher) {
		BaseEmailEvent event = occurrence.getOrigin();
		if (event.hasAttachment()) {
			for (String receiver : receivers.get(event.getType())) {
				sendAttachmentsMail(receiver,
						event.getEventLabel() + "@@" + event.getTitle(), event.getMsg(),
						event.getAttachment());
			}
		}
		else {
			for (String receiver : receivers.get(event.getType())) {
				sendEmail(receiver, event.getEventLabel() + "@@" + event.getTitle(),
						event.getMsg());
			}
		}
	}
}
