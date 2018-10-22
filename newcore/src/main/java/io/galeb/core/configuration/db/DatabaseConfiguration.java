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

import io.galeb.core.enums.SystemEnv;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class DatabaseConfiguration {

    /*
     * Hint: http://assets.en.oreilly.com/1/event/21/Connector_J%20Performance%20Gems%20Presentation.pdf
     */

    // @formatter:off
    private static final Integer DB_MAX_POOL_SIZE              = Integer.valueOf(SystemEnv.DB_MAX_POOL_SIZE.getValue());
    private static final Long    DB_CONN_TIMEOUT               = Long.valueOf(SystemEnv.DB_CONN_TIMEOUT.getValue());
    private static final Boolean DB_AUTOCOMMIT                 = Boolean.valueOf(SystemEnv.DB_AUTOCOMMIT.getValue());
    private static final Boolean DB_CACHE_PREP_STMTS           = Boolean.valueOf(SystemEnv.DB_CACHE_PREP_STMTS.getValue());
    private static final Integer DB_PREP_STMT_CACHE_SIZE       = Integer.valueOf(SystemEnv.DB_PREP_STMT_CACHE_SIZE.getValue());
    private static final Boolean DB_USE_SERVER_PREP_STMTS      = Boolean.valueOf(SystemEnv.DB_USE_SERVER_PREP_STMTS.getValue());
    private static final Integer DB_PREP_STMT_CACHE_SQL_LIMIT  = Integer.valueOf(SystemEnv.DB_PREP_STMT_CACHE_SQL_LIMIT.getValue());
    private static final Boolean DB_USE_LOCAL_SESSION_STATE    = Boolean.valueOf(SystemEnv.DB_USE_LOCAL_SESSION_STATE.getValue());
    private static final Boolean DB_REWRITE_BATCHED_STATEMENTS = Boolean.valueOf(SystemEnv.DB_REWRITE_BATCHED_STATEMENTS.getValue());
    private static final Boolean DB_CACHE_RESULT_SET_METADATA  = Boolean.valueOf(SystemEnv.DB_CACHE_RESULT_SET_METADATA.getValue());
    private static final Boolean DB_CACHE_SERVER_CONFIGURATION = Boolean.valueOf(SystemEnv.DB_CACHE_SERVER_CONFIGURATION.getValue());
    private static final Boolean DB_ELIDE_SET_AUTO_COMMITS     = Boolean.valueOf(SystemEnv.DB_ELIDE_SET_AUTO_COMMITS.getValue());
    private static final Boolean DB_MAINTAIN_TIME_STATS        = Boolean.valueOf(SystemEnv.DB_MAINTAIN_TIME_STATS.getValue());
    // @formatter:on

    @Bean
    @Primary
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public HikariDataSource dataSource(DataSourceProperties properties) {
        HikariDataSource hikariDataSource = (HikariDataSource) properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
        hikariDataSource.setConnectionTimeout(DB_CONN_TIMEOUT);
        hikariDataSource.setMaximumPoolSize(DB_MAX_POOL_SIZE);
        hikariDataSource.setAutoCommit(DB_AUTOCOMMIT);
        hikariDataSource.setConnectionTestQuery("SELECT 1");
        hikariDataSource.addDataSourceProperty("cachePrepStmts", DB_CACHE_PREP_STMTS);
        hikariDataSource.addDataSourceProperty("prepStmtCacheSize", DB_PREP_STMT_CACHE_SIZE);
        hikariDataSource.addDataSourceProperty("prepStmtCacheSqlLimit", DB_PREP_STMT_CACHE_SQL_LIMIT);
        hikariDataSource.addDataSourceProperty("useServerPrepStmts", DB_USE_SERVER_PREP_STMTS);
        hikariDataSource.addDataSourceProperty("useLocalSessionState", DB_USE_LOCAL_SESSION_STATE);
        hikariDataSource.addDataSourceProperty("rewriteBatchedStatements", DB_REWRITE_BATCHED_STATEMENTS);
        hikariDataSource.addDataSourceProperty("cacheResultSetMetadata", DB_CACHE_RESULT_SET_METADATA);
        hikariDataSource.addDataSourceProperty("cacheServerConfiguration", DB_CACHE_SERVER_CONFIGURATION);
        hikariDataSource.addDataSourceProperty("elideSetAutoCommits", DB_ELIDE_SET_AUTO_COMMITS);
        hikariDataSource.addDataSourceProperty("maintainTimeStats", DB_MAINTAIN_TIME_STATS);

        return hikariDataSource;
    }
}
