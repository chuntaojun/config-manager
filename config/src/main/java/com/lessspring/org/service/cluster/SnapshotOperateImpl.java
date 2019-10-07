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
package com.lessspring.org.service.cluster;

import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotReader;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotWriter;
import com.lessspring.org.DiskUtils;
import com.lessspring.org.raft.SnapshotOperate;
import com.lessspring.org.repository.SnapshotMapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Component(value = "snapshotOperate")
public class SnapshotOperateImpl implements SnapshotOperate {

    private Executor executor;

    private final String[] snapshotFiles = new String[]{
            "snapshot_config_info.csv",
            "snapshot_config_info_beta.csv",
            "snapshot_user.csv",
            "snapshot_user_role.csv",
            "snapshot_namespace.csv",
            "snapshot_namespace_permissions.csv"
    };

    @Resource
    private SnapshotMapper snapshotMapper;

    @PostConstruct
    public void init() {
        executor = Executors.newFixedThreadPool(1, r -> {
            Thread thread = new Thread(r);
            thread.setName("com.lessspring.org.Raft.doSnapshot");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public void onSnapshotSave(SnapshotWriter writer, Closure done) {
        final String path = writer.getPath() + File.separator;
        for (String fileName : snapshotFiles) {
            DiskUtils.deleteFile(path, fileName);
        }
        executor.execute(() -> snapshotMapper.doSnapshotSave(path));
    }

    @Override
    public boolean onSnapshotLoad(SnapshotReader reader) {
        final String path = reader.getPath() + File.separator;
        snapshotMapper.doSnapshotLoad(path);
        return true;
    }
}
