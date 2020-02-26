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
package com.conf.org.watch;

import java.util.Objects;
import java.util.Optional;

import com.conf.org.AbstractListener;
import com.conf.org.model.dto.ConfigInfo;
import com.conf.org.server.utils.PlaceholderProcessor;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public class WrapperListener extends AbstractListener {

	private static final PlaceholderProcessor processor = new PlaceholderProcessor();

	private String lastMd5;

	private final String encryption;

	private final AbstractListener listener;

	public WrapperListener(AbstractListener listener, String encryption) {
		this.listener = listener;
		this.encryption = encryption;
	}

	public String getLastMd5() {
		return lastMd5;
	}

	public void setLastMd5(String lastMd5) {
		this.lastMd5 = lastMd5;
	}

	public boolean isChange(String md5) {
		return !Objects.equals(lastMd5, md5);
	}

	/**
	 * @return the encryption
	 */
	public String getEncryption() {
		return encryption;
	}

	@Override
	public void onReceive(ConfigInfo configInfo) {
		processor.decryption(Optional.ofNullable(configInfo), encryption);
		configInfo.setEncryption(encryption);
		listener.onReceive(configInfo);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		WrapperListener that = (WrapperListener) o;
		return Objects.equals(listener, that.listener);
	}

	@Override
	public int hashCode() {
		return Objects.hash(listener);
	}
}
