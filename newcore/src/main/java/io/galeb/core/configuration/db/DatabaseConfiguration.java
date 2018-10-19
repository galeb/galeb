/*
 * Copyright (c) 2014-2018 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.core.configuration.db;

import com.zaxxer.hikari.HikariDataSource;
import java.util.Optional;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class DatabaseConfiguration {

    private static final String DB_MAX_POOL_SIZE             = Optional.ofNullable(System.getenv("DB_MAX_POOL_SIZE")).orElse("100");
    private static final String DB_CONN_TIMEOUT              = Optional.ofNullable(System.getenv("DB_CONN_TIMEOUT")).orElse("0");
    private static final String DB_AUTOCOMMIT                = Optional.ofNullable(System.getenv("DB_AUTOCOMMIT")).orElse("false");
    private static final String DB_CACHE_PREP_STMTS          = Optional.ofNullable(System.getenv("DB_CACHE_PREP_STMTS")).orElse("true");
    private static final String DB_PREP_STMT_CACHE_SIZE      = Optional.ofNullable(System.getenv("DB_PREP_STMT_CACHE_SIZE")).orElse("1024");
    private static final String DB_USE_SERVER_PREP_STMTS     = Optional.ofNullable(System.getenv("DB_USE_SERVER_PREP_STMTS")).orElse("true");
    private static final String DB_PREP_STMT_CACHE_SQL_LIMIT = Optional.ofNullable(System.getenv("DB_PREP_STMT_CACHE_SQL_LIMIT")).orElse("1024");

    @Bean
    @Primary
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public HikariDataSource dataSource(DataSourceProperties properties) {
        HikariDataSource hikariDataSource = (HikariDataSource) properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
        hikariDataSource.setConnectionTimeout(Long.parseLong(DB_CONN_TIMEOUT));
        hikariDataSource.setMaximumPoolSize(Integer.parseInt(DB_MAX_POOL_SIZE));
        hikariDataSource.setAutoCommit(Boolean.parseBoolean(DB_AUTOCOMMIT));
        hikariDataSource.setConnectionTestQuery("SELECT 1");
        hikariDataSource.addDataSourceProperty("cachePrepStmts", Boolean.parseBoolean(DB_CACHE_PREP_STMTS));
        hikariDataSource.addDataSourceProperty("prepStmtCacheSize", Integer.parseInt(DB_PREP_STMT_CACHE_SIZE));
        hikariDataSource.addDataSourceProperty("prepStmtCacheSqlLimit", Integer.parseInt(DB_PREP_STMT_CACHE_SQL_LIMIT));
        hikariDataSource.addDataSourceProperty("useServerPrepStmts", Boolean.parseBoolean(DB_USE_SERVER_PREP_STMTS));

        return hikariDataSource;
    }
}
