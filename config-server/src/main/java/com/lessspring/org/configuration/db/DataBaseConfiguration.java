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
package com.lessspring.org.configuration.db;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import com.lessspring.org.utils.PathConstants;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import okio.BufferedSource;
import okio.Okio;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
@Configuration
public class DataBaseConfiguration {

	@Autowired
	private PathConstants pathConstants;

	@Value("${com.lessspring.org.config.manager.db.name:lessSpring}")
	private String username;

	@Value("${com.lessspring.org.config.manager.db.password:lessSpring}")
	private String password;

	@Bean
	public DataSource dataSource() {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:h2:" + pathConstants.getParentPath() + File.separator
				+ "/db/config_manager");
		config.setUsername(username);
		config.setPassword(password);
		config.setMaximumPoolSize(10);
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		DataSource dataSource = new HikariDataSource(config);
		init(dataSource);
		return dataSource;
	}

	private void init(DataSource dataSource) {
		final String sql;
		try (InputStream stream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("config_manager.sql")) {
			assert stream != null;
			BufferedSource bufferedSource = Okio.buffer(Okio.source(stream));
			sql = bufferedSource.readUtf8();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (StringUtils.isEmpty(sql)) {
			throw new IllegalArgumentException("Database initialization scripts illegal");
		}
		try (Connection connection = dataSource.getConnection()) {
			Statement statement = connection.createStatement();
			statement.execute(sql);
			connection.commit();
		}
		catch (SQLException e) {
			log.error("load sql file and execute has error : {}", e.getMessage());
		}
	}

}
