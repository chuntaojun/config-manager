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
package com.lessspring.org.watch;

import com.lessspring.org.AbstractListener;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.utils.PlaceholderProcessor;

import java.util.Objects;
import java.util.Optional;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
class WrapperListener extends AbstractListener {

    private static final PlaceholderProcessor processor = new PlaceholderProcessor();

    private String lastMd5;

    private final AbstractListener listener;

    WrapperListener(AbstractListener listener) {
        this.listener = listener;
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

    @Override
    public void onReceive(ConfigInfo configInfo) {
        processor.decryption(Optional.ofNullable(configInfo));
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
