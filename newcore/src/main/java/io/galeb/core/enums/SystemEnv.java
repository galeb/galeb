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

package io.galeb.core.enums;

import io.galeb.core.so.Info;

import java.util.Optional;

/**
 * The enum System environments.
 */
@SuppressWarnings("unused")
public enum SystemEnv {
    // @formatter:off

    // COMMON

    /**
     * Group ID (same as color and/or cluster_id, if applicable).
     */
    GROUP_ID              ("GROUP_ID",              "UNDEF"),

    /**
     * Network ID.
     */
    ZONE_ID               ("ZONE_ID",               ""),

    /**
     * Syslog server host.
     */
    SYSLOG_HOST           ("SYSLOG_HOST",           "127.0.0.1"),

    /**
     * Syslog server port
     */
    SYSLOG_PORT           ("SYSLOG_PORT",           514),

    /**
     * Tags to separete logs
     */
    LOGGING_TAGS           ("LOGGING_TAGS",          "GALEB"),


    // HEALTHCHECKER

    /**
     * Service healthchecker port.
     */
    HEALTH_PORT              ("HEALTH_PORT",               7000),

    /**
     * AMQP Queue name
     */
    QUEUE_NAME               ("QUEUE_NAME",               "galeb-health"),

    /**
     * AMQP Queue name separator
     */
    QUEUE_NAME_SEPARATOR     ("QUEUE_NAME_SEPARATOR",     "-"),

    /**
     * Tester request timeout (ms)
     */
    TEST_CONN_TIMEOUT        ("TEST_CONN_TIMEOUT",        2000),

    /**
     * Tester request timeout (ms)
     */
    TEST_KEEPALIVE           ("TEST_KEEPALIVE",           true),

    /**
     * Tester pooled connection idle timeout (ms)
     */
    TEST_POOLED_IDLE_TIMEOUT ("TEST_POOLED_IDLE_TIMEOUT", 5),

    /**
     * Tester max conn per host
     */
    TEST_MAXCONN_PER_HOST    ("TEST_MAXCONN_PER_HOST",    100),

    /**
     * Broker Url Connection
     */
    BROKER_CONN              ("BROKER_CONN",              "tcp://localhost:61616?blockOnDurableSend=false&consumerWindowSize=0&protocols=Core"),

    /**
     * Broker user
     */
    BROKER_USER              ("BROKER_USER",              "guest"),

    /**
     * Broker password
     */
    BROKER_PASS              ("BROKER_PASS",              "guest"),

    /**
     * Enable JMS HA
     */
    BROKER_HA                ("BROKER_HA",                Boolean.FALSE),

    /**
     * Broker configuration of BlockOnDurableSend
     */
    BROKER_BLOCKDURABLESEND  ("BROKER_BLOCKDURABLESEND",  Boolean.FALSE),

    /**
     * Broker configuration of consumerWindowSize
     */
    BROKER_CONSUMERWINDOWSIZE ("BROKER_CONSUMERWINDOWSIZE", 0),

    /**
     * Set message's lifetime (in milliseconds) of the message when sending.
     */
    JMS_TIMEOUT              ("JMS_TIMEOUT",              30000),


    // ROUTER

    /**
     * Service router port.
     */
    ROUTER_PORT           ("ROUTER_PORT",           8000),

    /**
     * Galeb Manager Farm -> Environment Name
     */
    ENVIRONMENT_NAME      ("ENVIRONMENT_NAME",       ""),

    /**
     * Galeb Manager -> Environment Id
     */
    ENVIRONMENT_ID        ("ENVIRONMENT_ID",         0),

    /**
     * Galeb Manager URL
     */
    MANAGER_URL           ("MANAGER_URL",           "http://127.0.0.1:8000"),

    /**
     * Galeb Manager Map Path
     */
    MANAGER_MAP_PATH      ("MANAGER_MAP_PATH",      "/virtualhostscached/"),

    /**
     * Galeb Manager Routers Path
     */
    MANAGER_ROUTERS_PATH  ("MANAGER_ROUTERS_PATH",  "/routers"),

    /**
     * Statsd prefix.
     */
    STATSD_PREFIX         ("STATSD_PREFIX",         "galeb"),

    /**
     * Statsd server host.
     */
    STATSD_HOST           ("STATSD_HOST",           "127.0.0.1"),

    /**
     * Statsd server port.
     */
    STATSD_PORT           ("STATSD_PORT",           8125),

    /**
     * Specify the number of I/O threads to create for the worker. If not specified, a default will be chosen.
     */
    IO_THREADS            ("IO_THREADS",            Math.max(Runtime.getRuntime().availableProcessors(), 2)),

    /**
     * Specify the number of "core" threads for the worker task thread pool.
     */
    WORKER_THREADS        ("WORKER_THREADS",        Integer.parseInt(IO_THREADS.getValue()) * 8),

    /**
     * The buffer size to use.
     */
    BUFFER_SIZE           ("BUFFER_SIZE",           16384),

    /**
     * Specify whether direct buffers should be used for socket communications.
     */
    DIRECT_BUFFER         ("DIRECT_BUFFER",         Boolean.TRUE),

    /**
     * Configure a server with the specified backlog. If unset then uses SO backlog.
     */
    BACKLOG               ("BACKLOG",               Info.getSOBacklog()),

    /**
     * The timeout for idle connections. Note that if POOL_SOFTMAXCONN > 0 then once the pool is down
     * to the core size no more connections will be timed out.
     */
    POOL_CONN_TTL         ("POOL_CONN_TTL",         10000),

    /**
     * The minimum number of connections that this proxy connection pool will try and keep established. Once the pool
     * is down to this number of connections no more connections will be timed out.
     * NOTE: This value is per IO thread, so to get the actual value this must be multiplied by the number of IO threads
     */
    POOL_SOFTMAXCONN      ("POOL_SOFTMAXCONN",      0),

    /**
     * The maximum number of connections that can be established to the target
     */
    POOL_MAXCONN          ("POOL_MAXCONN",          2000),

    /**
     * The maximum amount of time to allow the request to be processed
     */
    POOL_MAX_REQUEST_TIME ("POOL_MAX_REQUEST_TIME", -1),

    /**
     *  The frontend Max Connections
     */
    FE_MAXCONN            ("FE_MAXCONN",              20000),

    /**
     * Enable AccessLog (http response register)
     */
    ENABLE_ACCESSLOG      ("ENABLE_ACCESSLOG",      Boolean.TRUE),

    /**
     * Enable send metrics to statsd server. See STATSD_HOST and STATSD_PORT.
     */
    ENABLE_STATSD         ("ENABLE_STATSD",         Boolean.TRUE),

    /**
     * Get and send to statsd the Target(Backend) openConnections counter.
     */
    SEND_OPENCONN_COUNTER ("SEND_OPENCONN_COUNTER", Boolean.TRUE),

    /**
     * Should any existing X-Forwarded-For header be used or should it be overwritten.
     */
    REUSE_XFORWARDED      ("REUSE_XFORWARDED",      Boolean.TRUE),

    /**
     * Should the HOST header be rewritten to use the target host of the call.
     */
    REWRITE_HOST_HEADER   ("REWRITE_HOST_HEADER",   Boolean.FALSE),

    /**
     * Request header to store a UUIDv4 representing the current request, if not already set.
     */
    REQUESTID_HEADER      ("REQUESTID_HEADER",      ""),

    /**
     * HashSourceIpHostSelector exclusive use. Dont uses Header X-FORWARDED-FOR as Hash key.
     */
    IGNORE_XFORWARDED_FOR ("IGNORE_XFORWARDED_FOR", Boolean.FALSE),

    /**
     * External Data provider tcp max connections
     */
    EXTERNALDATA_MAXCONN  ("EXTERNALDATA_MAXCONN",  10),

    /**
     * Extenal Data provider connections pool size
     */
    EXTERNALDATA_POOL     ("EXTERNALDATA_POOL",     5),

    /**
     * External Data provider connection timeout
     */
    EXTERNALDATA_TIMEOUT  ("EXTERNALDATA_TIMEOUT",  10000),

    /**
     * Enable JMX reporter (Undertow front-end metrics)
     */
    ENABLE_UNDERTOW_JMX   ("ENABLE_UNDERTOW_JMX",  Boolean.TRUE),


    // MANAGER/*

    /**
     * Cache expiration (seconds)
     */
    CACHE_EXPIRATION              ("CACHE_EXPIRATION",              300),

    /**
     * The property controls the maximum size that the pool is allowed to reach, including both idle and in-use
     * connections. Basically this value will determine the maximum number of actual connections to the database
     * backend.
     *
     * When the pool reaches this size, and no idle connections are available, calls to getConnection() will
     * block for up to connectionTimeout milliseconds before timing out.
     */
    DB_MAX_POOL_SIZE              ("DB_MAX_POOL_SIZE",              100),

    /**
     * Set the maximum number of milliseconds that a client will wait for a connection from the pool. If this
     * time is exceeded without a connection becoming available, a SQLException will be thrown from
     */
    DB_CONN_TIMEOUT               ("DB_CONN_TIMEOUT",               0L),

    /**
     * The property controls the maximum lifetime of a connection in the pool. An in-use connection will never
     * be retired, only when it is closed will it then be removed. On a connection-by-connection basis, minor
     * negative attenuation is applied to avoid mass-extinction in the pool.
     */
    DB_MAX_LIFE_TIME               ("DB_MAX_LIFE_TIME",             1800000),

    /**
     * Set the default auto-commit behavior of connections in the pool.
     */
    DB_AUTOCOMMIT                 ("DB_AUTOCOMMIT",                 Boolean.FALSE),

    /**
     * Enable Cache prepared statements
     */
    DB_CACHE_PREP_STMTS           ("DB_CACHE_PREP_STMTS",           Boolean.TRUE),

    /**
     * This sets the number of prepared statements that the MySQL driver will cache per connection.
     * The default is a conservative 25. We recommend setting this to between 250-500.
     */
    DB_PREP_STMT_CACHE_SIZE       ("DB_PREP_STMT_CACHE_SIZE",       500),

    /**
     * Newer versions of MySQL support server-side prepared statements,
     * this can provide a substantial performance boost
     */
    DB_USE_SERVER_PREP_STMTS      ("DB_USE_SERVER_PREP_STMTS",      Boolean.TRUE),

    /**
     * This is the maximum length of a prepared SQL statement that the driver will cache.
     * The MySQL default is 256. In our experience, especially with ORM frameworks like Hibernate,
     * this default is well below the threshold of generated statement lengths. Our recommended setting is 2048.
     */
    DB_PREP_STMT_CACHE_SQL_LIMIT  ("DB_PREP_STMT_CACHE_SQL_LIMIT",  2048),

    /**
     * Should the driver refer to the internal values of autocommit and transaction isolation that are set by
     * Connection.setAutoCommit() and Connection.setTransactionIsolation() and transaction state as maintained by
     * the protocol, rather than querying the database or blindly sending commands to the database for commit()
     * or rollback() method calls?
     */
    DB_USE_LOCAL_SESSION_STATE    ("DB_USE_LOCAL_SESSION_STATE",    Boolean.TRUE),

    /**
     * Should the driver use multiqueries (irregardless of the setting of "allowMultiQueries") as well as
     * rewriting of prepared statements for INSERT into multi-value inserts when executeBatch() is called?
     */
    DB_REWRITE_BATCHED_STATEMENTS ("DB_REWRITE_BATCHED_STATEMENTS", Boolean.TRUE),

    /**
     *  Should the driver cache ResultSetMetaData for Statements and PreparedStatements?
     */
    DB_CACHE_RESULT_SET_METADATA  ("DB_CACHE_RESULT_SET_METADATA",  Boolean.TRUE),

    /**
     * Should the driver cache the results of 'SHOW VARIABLES' and 'SHOW COLLATION' on a per-URL basis?
     */
    DB_CACHE_SERVER_CONFIGURATION ("DB_CACHE_SERVER_CONFIGURATION", Boolean.TRUE),

    /**
     * If using MySQL-4.1 or newer, should the driver only issue 'set autocommit=n' queries
     * when the server's state doesn't match the requested state by Connection.setAutoCommit(boolean)?
     */
    DB_ELIDE_SET_AUTO_COMMITS     ("DB_ELIDE_SET_AUTO_COMMITS",     Boolean.TRUE),

    /**
     * Should the driver maintain various internal timers to enable idle time calculations as well as more
     * verbose error messages when the connection to the server fails?
     * Setting this property to false removes at least two calls to System.getCurrentTimeMillis() per query.
     */
    DB_MAINTAIN_TIME_STATS        ("DB_MAINTAIN_TIME_STATS",        Boolean.FALSE),

    // MANAGER/LEGBA

    /**
     *
     */
    REGISTER_ROUTER_TTL           ("REGISTER_ROUTER_TTL",           30000L),

    /**
     * route map (cache) version ttl (milliseconds)
     */
    LEGBA_CACHE_EXPIRATION        ("LEGBA_CACHE_EXPIRATION",        60000L),

    /**
     * Enable discovered members size
     */
    ENABLE_DISCOVERED_MEMBERS_SIZE ("ENABLE_DISCOVERED_MEMBERS_SIZE", Boolean.FALSE);

    // @formatter:on

    /**
     * Gets SystemEnv value.
     *
     * @return the enum value
     */
    public String getValue() {
        return value;
    }

    private final String value;

    SystemEnv(String env, Object def) {
        this.value = Optional.ofNullable(System.getenv(env)).orElse(String.valueOf(def));
    }
}
