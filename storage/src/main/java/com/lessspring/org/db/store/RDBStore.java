///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.lessspring.org.db.store;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//import javax.sql.DataSource;
//
//import com.lessspring.org.LifeCycle;
//import com.lessspring.org.db.RDBStoreException;
//import com.zaxxer.hikari.HikariConfig;
//import com.zaxxer.hikari.HikariDataSource;
//import lombok.extern.slf4j.Slf4j;
//import okio.BufferedSource;
//import okio.Okio;
//import org.apache.commons.lang3.StringUtils;
//
///**
// * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
// * @since 0.0.1
// */
//@Slf4j
//public class RDBStore implements LifeCycle {
//
//	private StoreEngineOptions options;
//	private DataSource dataSource;
//
//	private final AtomicBoolean inited = new AtomicBoolean(false);
//	private final AtomicBoolean destroyed = new AtomicBoolean(false);
//
//	public RDBStore(StoreEngineOptions options) {
//		this.options = options;
//	}
//
//	public DataSource getDataSource() {
//		return dataSource;
//	}
//
//	@Override
//	public void init() {
//		if (inited.compareAndSet(false, true)) {
//			buildStore();
//		}
//	}
//
//	@Override
//	public void destroy() {
//		if (isInited() && destroyed.compareAndSet(false, true)) {
//		}
//	}
//
//	private void buildStore() {
//		HikariConfig config = new HikariConfig();
//		config.setJdbcUrl("jdbc:h2:" + options.getCacheDir() + File.separator
//				+ "/db/config_manager");
//		config.setUsername(options.getDbUsername());
//		config.setPassword(options.getDbPassword());
//		config.setMaximumPoolSize(options.getMaxPoolSize());
//		config.addDataSourceProperty("cachePrepStmts", "true");
//		config.addDataSourceProperty("prepStmtCacheSize", "250");
//		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
//		this.dataSource = new HikariDataSource(config);
//		logNodeDBInit(dataSource);
//	}
//
//	private void logNodeDBInit(DataSource dataSource) {
//		final String sql;
//		try (InputStream stream = Thread.currentThread().getContextClassLoader()
//				.getResourceAsStream("config_manager.sql")) {
//			assert stream != null;
//			BufferedSource bufferedSource = Okio.buffer(Okio.source(stream));
//			sql = bufferedSource.readUtf8();
//		}
//		catch (IOException e) {
//			throw new RDBStoreException(e);
//		}
//		if (StringUtils.isEmpty(sql)) {
//			throw new RDBStoreException(new IllegalArgumentException(
//					"Database initialization scripts illegal"));
//		}
//		try (Connection connection = dataSource.getConnection()) {
//			Statement statement = connection.createStatement();
//			statement.execute(sql);
//			connection.commit();
//		}
//		catch (SQLException e) {
//			log.error("load sql file and execute has error : {}", e.getMessage());
//		}
//	}
//
//	@Override
//	public boolean isInited() {
//		return inited.get();
//	}
//
//	@Override
//	public boolean isDestroyed() {
//		return destroyed.get();
//	}
//}
