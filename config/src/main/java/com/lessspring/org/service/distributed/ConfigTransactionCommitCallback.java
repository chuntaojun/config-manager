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
package com.lessspring.org.service.distributed;

import com.lessspring.org.raft.OperationEnum;
import com.lessspring.org.raft.Transaction;
import com.lessspring.org.raft.TransactionCommitCallback;
import com.lessspring.org.service.config.PersistentHandler;
import com.lessspring.org.utils.PropertiesEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component(value = "configTransactionCommitCallback")
public class ConfigTransactionCommitCallback implements TransactionCommitCallback {

    private final HashMap<OperationEnum, Consumer<Transaction>> consumerMap = new HashMap<>();

    public ConfigTransactionCommitCallback() {
    }

    public void registerConsumer(Consumer<Transaction> consumer, OperationEnum operation) {
        synchronized (this) {
            consumerMap.put(operation, consumer);
        }
    }

    @Override
    public void onApply(Transaction transaction) {
        consumerMap.get(transaction.getOperation()).accept(transaction);
    }

    @Override
    public boolean interest(String trsKey) {
        return trsKey.contains(PropertiesEnum.InterestKey.CONFIG_DARA.getType());
    }
}
