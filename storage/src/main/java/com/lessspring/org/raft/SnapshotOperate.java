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
import com.alipay.sofa.jraft.entity.LocalFileMetaOutter;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotReader;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotWriter;
import com.google.protobuf.ByteString;
import com.lessspring.org.server.utils.GsonUtils;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public interface SnapshotOperate {

	/**
	 * do snapshot save operation
	 *
	 * @param writer {@link SnapshotWriter}
	 * @param done {@link Closure}
	 */
	void onSnapshotSave(SnapshotWriter writer, Closure done);

	/**
	 * do snapshot load operation
	 *
	 * @param reader {@link SnapshotReader}
	 * @return operation label
	 */
	boolean onSnapshotLoad(SnapshotReader reader);

	/**
	 *
	 *
	 * @param metadata
	 * @param <T>
	 * @return
	 */
	default <T> LocalFileMetaOutter.LocalFileMeta buildMetadata(final T metadata) {
		return metadata == null ? null
				: LocalFileMetaOutter.LocalFileMeta.newBuilder()
				.setUserMeta(ByteString.copyFrom(GsonUtils.toJsonBytes(metadata)))
				.build();
	}

}
