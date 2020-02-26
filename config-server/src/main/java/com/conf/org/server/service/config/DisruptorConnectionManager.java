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

package com.conf.org.server.service.config;

import com.conf.org.raft.pojo.TransactionId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@Slf4j
@Component
public class DisruptorConnectionManager {

    private ThreadLocal<String> localXID = new ThreadLocal<>();

    @Autowired
    private ConfigTransactionIdManager idManager;

    @Autowired
    private DataSource dataSource;

    private final Map<String, ConnectionHolder> connectionMap = new ConcurrentHashMap<>();

    private TransactionId transactionId;

    @PostConstruct
    protected void init() {
        transactionId = new TransactionId("xid");
        idManager.register(transactionId);
    }

    public String createXID() {
        String xid = transactionId.increaseAndObtain().toString();
        connectionMap.computeIfAbsent(xid, s -> {
            try {
                final ConnectionHolder holder = new ConnectionHolder();
                holder.connection = dataSource.getConnection();
                holder.savepoint = holder.connection.setSavepoint();
                return holder;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        localXID.set(xid);
        return xid;
    }

    private ConnectionHolder getConnection(String xid) {
        return connectionMap.get(xid);
    }

    public void commit() {
        commit(localXID.get());
        localXID.remove();
    }

    public void rollback() {
        rollback(localXID.get());
        localXID.remove();
    }


    public void commit(String xid) {
        Optional.ofNullable(getConnection(xid))
                .ifPresent(connectionHolder -> {
                    try {
                        connectionHolder.connection.commit();
                    } catch (Exception e) {
                        log.error("commit has error : {}, xid : {}", e, xid);
                    }
                });
    }

    public void rollback(String xid) {
        Optional.ofNullable(getConnection(xid))
                .ifPresent(connectionHolder -> {
                    try {
                        final Savepoint savepoint = connectionHolder.savepoint;
                        connectionHolder.connection.rollback(savepoint);
                    } catch (Exception e) {
                        log.error("rollback has error : {}, xid : {}", e, xid);
                    }
                });
    }

    private class ConnectionHolder {

        private Connection connection;
        private Savepoint savepoint;

    }

}
