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

import com.alipay.remoting.rpc.RpcServer;
import com.alipay.sofa.jraft.CliService;
import com.alipay.sofa.jraft.JRaftUtils;
import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.RaftGroupService;
import com.alipay.sofa.jraft.RouteTable;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.entity.Task;
import com.alipay.sofa.jraft.option.CliOptions;
import com.alipay.sofa.jraft.option.NodeOptions;
import com.alipay.sofa.jraft.rpc.RaftRpcServerFactory;
import com.alipay.sofa.jraft.rpc.impl.cli.BoltCliClientService;
import com.google.common.eventbus.Subscribe;
import com.lessspring.org.LifeCycle;
import com.lessspring.org.PathUtils;
import com.lessspring.org.SerializerUtils;
import com.lessspring.org.event.ServerNodeChangeEvent;
import com.lessspring.org.model.vo.ResponseData;
import com.lessspring.org.raft.dto.Datum;
import com.lessspring.org.raft.vo.ServerNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.checkerframework.checker.units.qual.C;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
public class ClusterServer implements LifeCycle {

    private static final String SERVER_NODE_SELF_INDEX = "cluster.server.node.self.index";
    private static final String SERVER_NOED_IP = "cluster.server.node.ip.";
    private static final String SERVER_NOED_PORT = "cluster.server.node.port.";
    private static final String CACHE_DIR_PATH = "config_manager_raft";

    private NodeManager nodeManager = NodeManager.getInstance();
    private RaftServer raftServer = new RaftServer();

    @Override
    public void init() {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("cluster.properties")) {
            Properties properties = new Properties();
            properties.load(is);
            initClusterNode(properties);
            raftServer.initRaftCluster(nodeManager, CACHE_DIR_PATH);
        } catch (IOException e) {
            log.error("Server");
        }
    }

    private void initClusterNode(Properties properties) {
        int nodes = properties.size() / 2;
        int selfIndex = Integer.parseInt(properties.getProperty(SERVER_NODE_SELF_INDEX, "0"));
        for (int i = 0; i < nodes; i ++) {
            String ip = properties.getProperty(SERVER_NOED_IP + i);
            String port = properties.getProperty(SERVER_NOED_PORT + i);
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

    public CompletableFuture<ResponseData> apply(Datum datum) {
        Task task = new Task();
        task.setData(ByteBuffer.wrap(SerializerUtils.getInstance().serialize(datum)));
        task.setDone(new DatumStoreClosure(datum, status -> {
            Result result = new Result();
            result.setOk(status.isOk());
            result.setErrorMsg(status.getErrorMsg());
            future.complete(result);
        }));
    }

    @Override
    public void destroy() {

    }

    @Subscribe
    public void onChange(ServerNodeChangeEvent nodeChangeEvent) {

    }

}
