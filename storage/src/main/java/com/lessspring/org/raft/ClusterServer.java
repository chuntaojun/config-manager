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


import com.alipay.remoting.InvokeCallback;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.sofa.jraft.entity.Task;
import com.google.common.eventbus.Subscribe;
import com.lessspring.org.LifeCycle;
import com.lessspring.org.SerializerUtils;
import com.lessspring.org.event.ServerNodeChangeEvent;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.raft.dto.Datum;
import com.lessspring.org.raft.vo.ServerNode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@SuppressWarnings("unchecked")
public class ClusterServer implements LifeCycle {

    private static final String SERVER_NODE_SELF_INDEX = "cluster.server.node.self.index";
    private static final String SERVER_NODE_IP = "cluster.server.node.ip.";
    private static final String SERVER_NODE_PORT = "cluster.server.node.port.";
    private static final String CACHE_DIR_PATH = "config_manager_raft";
    private AtomicBoolean initialize = new AtomicBoolean(false);

    private NodeManager nodeManager = NodeManager.getInstance();
    private RaftServer raftServer = new RaftServer();

    @Override
    public void init() {
        if (initialize.compareAndSet(false, true)) {
            try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("cluster.properties")) {
                Properties properties = new Properties();
                properties.load(is);
                initClusterNode(properties);
                raftServer.init();
                raftServer.initRaftCluster(nodeManager, CACHE_DIR_PATH);
            } catch (IOException e) {
                log.error("Server");
                initialize.lazySet(false);
            }
        }
    }

    private void initClusterNode(Properties properties) {
        int nodes = properties.size() / 2;
        int selfIndex = Integer.parseInt(properties.getProperty(SERVER_NODE_SELF_INDEX, "0"));
        for (int i = 0; i < nodes; i ++) {
            String ip = properties.getProperty(SERVER_NODE_IP + i);
            String port = properties.getProperty(SERVER_NODE_PORT + i);
            ServerNode node = ServerNode.builder()
                    .nodeIp(ip)
                    .port(Integer.parseInt(port))
                    .build();
            if (i == selfIndex) {
                nodeManager.setSelf(node);
            }
            nodeManager.nodeJoin(node);
        }
    }

    public void registerTransactionCommitCallback(TransactionCommitCallback commitCallback) {
        raftServer.registerTransactionCommitCallback(commitCallback);
    }

    public CompletableFuture<ResponseData<Boolean>> apply(Datum datum) throws RemotingException, InterruptedException {
        needInitialized();
        final Throwable[] throwables = new Throwable[]{null};
        CompletableFuture<ResponseData<Boolean>> future = new CompletableFuture<>();
        Task task = new Task();
        task.setData(ByteBuffer.wrap(SerializerUtils.getInstance().serialize(datum)));
        task.setDone(new ConfigStoreClosure(datum, status -> {
            ResponseData<Boolean> data = ResponseData.builder()
                    .withCode(status.getCode())
                    .withData(status.isOk())
                    .withErrMsg(status.getErrorMsg())
                    .build();
            future.complete(data);
            if (!status.isOk()) {
                throwables[0] = new RuntimeException(status.getErrorMsg());
            }
        }));
        if (raftServer.isLeader()) {
            raftServer.getNode().apply(task);
        } else {
            raftServer.getCliClientService().getRpcClient().invokeWithCallback(raftServer.leaderIp(), datum, new InvokeCallback() {
                @Override
                public void onResponse(Object o) {
                    ResponseData<Boolean> data = ResponseData.success();
                    future.complete(data);
                }

                @Override
                public void onException(Throwable throwable) {
                    throwables[0] = throwable;
                }

                @Override
                public Executor getExecutor() {
                    return null;
                }
            }, 5000);
        }
        if (Objects.nonNull(throwables[0])) {
            throw new RemotingException("Commit Error", throwables[0]);
        }
        return future;
    }

    @Override
    public void destroy() {
        raftServer.destroy();
    }

    @Subscribe
    public void onChange(ServerNodeChangeEvent nodeChangeEvent) {
        needInitialized();
    }

    private void needInitialized() {
        if (!initialize.get()) {
            throw new IllegalStateException("Uninitialized cluster");
        }
    }

}
