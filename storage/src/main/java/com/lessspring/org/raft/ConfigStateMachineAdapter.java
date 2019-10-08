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
package com.lessspring.org.raft;

import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Iterator;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.core.StateMachineAdapter;
import com.alipay.sofa.jraft.error.RaftError;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotReader;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotWriter;
import com.lessspring.org.SerializerUtils;
import com.lessspring.org.raft.dto.Datum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
public class ConfigStateMachineAdapter extends RaftStateMachineAdaper {

    private final List<TransactionCommitCallback> callbacks = new LinkedList<>();

    private final SerializerUtils serializer = SerializerUtils.getInstance();

    private final Object monitor = new Object();

    private SnapshotOperate snapshotOperate;

    @Override
    public void onApply(Iterator iter) {
        int index = 0;
        int applied = 0;
        try {
            while (iter.hasNext()) {
                Datum datum = null;
                ConfigStoreClosure closure = null;
                try {
                    if (iter.done() != null) {
                        closure = (ConfigStoreClosure) iter.done();
                        datum = closure.getDatum();
                    } else {
                        final ByteBuffer data = iter.getData();
                        datum = serializer.deserialize(data.array(), Datum.class);
                    }
                    final Transaction transaction = new Transaction(datum.getKey(),
                            datum.getValue(), datum.getOperationEnum());
                    // For each transaction, according to the different processing of
                    // the key to the callback interface
                    callbacks.forEach(commitCallback -> {
                        if (commitCallback.interest(transaction.getKey())) {
                            commitCallback.onApply(transaction);
                        }
                    });
                } catch (Throwable e) {
                    index++;
                    throw new RuntimeException("Decode operation error", e);
                }
                if (Objects.nonNull(closure)) {
                    closure.run(Status.OK());
                }
                applied ++;
                index ++;
                iter.next();
            }
        } catch (Throwable t) {
            iter.setErrorAndRollback(index - applied, new Status(RaftError.ESTATEMACHINE,
                    "StateMachine meet critical error: %s.", t.getMessage()));
        }
    }

    @Override
    public void onSnapshotSave(SnapshotWriter writer, Closure done) {
        snapshotOperate.onSnapshotSave(writer, done);
    }

    @Override
    public boolean onSnapshotLoad(SnapshotReader reader) {
        return snapshotOperate.onSnapshotLoad(reader);
    }

    @Override
    public void registerTransactionCommitCallback(TransactionCommitCallback commitCallback) {
        synchronized (monitor) {
            callbacks.add(commitCallback);
        }
    }

    @Override
    public void registerSnapshotManager(SnapshotOperate snapshotOperate) {
        this.snapshotOperate = snapshotOperate;
    }
}
