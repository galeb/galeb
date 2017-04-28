/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
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

package io.galeb.core.configuration;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SuppressWarnings("unused")
public enum SystemEnvs {

    // COMMON

    /**
     * Cluster ID (same as Farm ID).
     */
    CLUSTER_ID          (System.getenv("CLUSTER_ID"),           "GALEB"),

    /**
     * Syslog server host.
     */
    SYSLOG_HOST         (System.getenv("SYSLOG_HOST"),          "127.0.0.1"),

    /**
     * Syslog server port
     */
    SYSLOG_PORT         (System.getenv("SYSLOG_PORT"),          514),

    /**
     *  Galeb Manager URL
     */
    MANAGER_URL         (System.getenv("MANAGER_URL"),          "http://127.0.0.1:8000"),

    /**
     *  Galeb Manager user
     */
    MANAGER_USER        (System.getenv("MANAGER_USER"),         "user"),

    /**
     *  Galeb Manager password
     */
    MANAGER_PASS        (System.getenv("MANAGER_PASS"),         "password"),

    /**
     *  Queuer CONN
     */
    QUEUE_CONN          (System.getenv("QUEUE_CONN"),           "tcp://localhost:61616?protocols=Core"),


    // HEALTHCHECKER

    /**
     * Service healthchecker port.
     */
    HEALTH_PORT         (System.getenv("HEALTH_PORT"),          7000),

    /**
     *  Enable JMS HA
     */
    ENABLE_HA           (System.getenv("ENABLE_HA"),            Boolean.FALSE),

    /**
     *  AMQP Queue name
     */
    QUEUE_NAME          (System.getenv("QUEUE_NAME"),           "galeb-health"),

    /**
     *  Tester request timeout (ms)
     */
    TEST_CONN_TIMEOUT   (System.getenv("TEST_CONN_TIMEOUT"),    2000),


    // ROUTER

    /**
     * Service router port.
     */
    ROUTER_PORT           (System.getenv("ROUTER_PORT"),           8000),

    /**
     *  Galeb Manager Farm -> Environment Name
     */
    ENVIRONMENT_NAME    (System.getenv("ENVIRONMENT_NAME"),     ""),

    /**
     * Etcd API full url (schema+host:port).
     */
    ETCD_SERVER           (System.getenv("ETCD_SERVER"),           "http://127.0.0.1:2379"),

    /**
     * Statsd prefix.
     */
    STATSD_PREFIX         (System.getenv("STATSD_PREFIX"),         CLUSTER_ID.getValue()),

    /**
     * Statsd server host.
     */
    STATSD_HOST           (System.getenv("STATSD_HOST"),           "127.0.0.1"),

    /**
     * Statsd server port.
     */
    STATSD_PORT           (System.getenv("STATSD_PORT"),           8125),

    /**
     * Specify the number of I/O threads to create for the worker. If not specified, a default will be chosen.
     */
    IO_THREADS            (System.getenv("IO_THREADS"),            Math.max(Runtime.getRuntime().availableProcessors(), 2)),

    /**
     * Specify the number of "core" threads for the worker task thread pool.
     */
    WORKER_THREADS        (System.getenv("WORKER_THREADS"),        Integer.parseInt(IO_THREADS.getValue()) * 8),

    /**
     * The buffer size to use.
     */
    BUFFER_SIZE           (System.getenv("BUFFER_SIZE"),           16384),

    /**
     * Specify whether direct buffers should be used for socket communications.
     */
    DIRECT_BUFFER         (System.getenv("DIRECT_BUFFER"),         Boolean.TRUE),

    /**
     * Configure a server with the specified backlog. If unset then uses SO backlog.
     */
    BACKLOG               (System.getenv("BACKLOG"),               getSOBacklog()),

    /**
     * The timeout for idle connections. Note that if POOL_SOFTMAXCONN > 0 then once the pool is down
     * to the core size no more connections will be timed out.
     */
    POOL_CONN_TTL         (System.getenv("POOL_CONN_TTL"),         10000),

    /**
     * The minimum number of connections that this proxy connection pool will try and keep established. Once the pool
     * is down to this number of connections no more connections will be timed out.
     *
     * NOTE: This value is per IO thread, so to get the actual value this must be multiplied by the number of IO threads
     */
    POOL_SOFTMAXCONN      (System.getenv("POOL_SOFTMAXCONN"),      0),

    /**
     * The maximum number of connections that can be established to the target
     */
    POOL_CONN_PER_THREAD  (System.getenv("POOL_CONN_PER_THREAD"),  2000),

    /**
     * The maximum amount of time to allow the request to be processed
     */
    POOL_MAX_REQUEST_TIME (System.getenv("POOL_MAX_REQUEST_TIME"), -1),

    /**
     * Enable AccessLog (http response register)
     */
    ENABLE_ACCESSLOG      (System.getenv("ENABLE_ACCESSLOG"),      Boolean.TRUE),

    /**
     * Enable send metrics to statsd server. See STATSD_HOST and STATSD_PORT.
     */
    ENABLE_STATSD         (System.getenv("ENABLE_STATSD"),         Boolean.TRUE),

    /**
     * Get and send to statsd the Target(Backend) openConnections counter.
     */
    SEND_OPENCONN_COUNTER (System.getenv("SEND_OPENCONN_COUNTER"), Boolean.TRUE),

    /**
     * Should any existing X-Forwarded-For header be used or should it be overwritten.
     */
    REUSE_XFORWARDED      (System.getenv("REUSE_XFORWARDED"),      Boolean.TRUE),

    /**
     * Should the HOST header be rewritten to use the target host of the call.
     */
    REWRITE_HOST_HEADER   (System.getenv("REWRITE_HOST_HEADER"),   Boolean.FALSE),

    /**
     * HashSourceIpHostSelector exclusive use. Dont uses Header X-FORWARDED-FOR as Hash key.
     */
    IGNORE_XFORWARDED_FOR (System.getenv("IGNORE_XFORWARDED_FOR"), Boolean.FALSE),

    /**
     * Consistent Hash number of replicas (HashHostSelector exclusive use).
     */
    HASH_NUM_REPLICAS     (System.getenv("HASH_NUM_REPLICAS"),     100),

    /**
     * Consistent Hash Algorithm (HashHostSelector exclusive use).
     */
    HASH_ALGORITHM        (System.getenv("HASH_ALGORITHM"),        "MURMUR3_32"),

    /**
     *  External Data provider tcp max connections
     */
    EXTERNALDATA_MAXCONN  (System.getenv("EXTERNALDATA_MAXCONN"),  1),

    /**
     *  Extenal Data provider connections pool size
     */
    EXTERNALDATA_POOL     (System.getenv("EXTERNALDATA_POOL"),     1),

    /**
     *  External Data provider connection timeout
     */
    EXTERNALDATA_TIMEOUT  (System.getenv("EXTERNALDATA_TIMEOUT"),  10000),

    /**
     *  Enable JMX reporter (Undertow front-end metrics)
     */
    ENABLE_UNDERTOW_JMX   (System.getenv("EXTERNALDATA_TIMEOUT"),  Boolean.TRUE);

    private static int getSOBacklog() {
        int tcp_max_syn_backlog = 1000;
        if (System.getProperty("os.name", "UNDEF").toLowerCase().startsWith("linux")) {
            try {
                final Path tcp_max_syn_backlog_file = Paths.get("/proc/sys/net/ipv4/tcp_max_syn_backlog");
                tcp_max_syn_backlog = Integer.parseInt(new String(Files.readAllBytes(tcp_max_syn_backlog_file), Charset.defaultCharset()));
            } catch (IOException e) {
                return tcp_max_syn_backlog;
            }
        }
        return tcp_max_syn_backlog;
    }

    public String getValue() {
        return value;
    }

    private final String value;

    SystemEnvs(String env, Object def) {
        this.value = env != null ? env : String.valueOf(def);
    }
}
