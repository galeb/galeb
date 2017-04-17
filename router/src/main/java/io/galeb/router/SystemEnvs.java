package io.galeb.router;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SuppressWarnings("unused")
public enum SystemEnvs {

    /**
     * Cluster ID (same as Farm ID).
     */
    CLUSTER_ID            (System.getenv("CLUSTER_ID"),            "GALEB"),

    /**
     * Service router port.
     */
    ROUTER_PORT           (System.getenv("ROUTER_PORT"),           8000),

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
     * Syslog server host.
     */
    SYSLOG_HOST           (System.getenv("SYSLOG_HOST"),           "127.0.0.1"),

    /**
     * Syslog server port
     */
    SYSLOG_PORT           (System.getenv("SYSLOG_PORT"),           514),

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
    POOL_CONN_TTL         (System.getenv("POOL_CONN_TTL"),         -1),

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
    HASH_ALGORITHM        (System.getenv("HASH_ALGORITHM"),        "MURMUR3_32");

    private static int getSOBacklog() {
        int tcp_max_syn_backlog = 1000;
        if (System.getProperty("os.name", "UNDEF").toLowerCase().startsWith("linux")) {
            try {
                registerPid();
                final Path tcp_max_syn_backlog_file = Paths.get("/proc/sys/net/ipv4/tcp_max_syn_backlog");
                tcp_max_syn_backlog = Integer.parseInt(new String(Files.readAllBytes(tcp_max_syn_backlog_file), Charset.defaultCharset()));
            } catch (IOException e) {
                return tcp_max_syn_backlog;
            }
        }
        return tcp_max_syn_backlog;
    }

    private static void registerPid() throws IOException {
        String contentPid = new String(Files.readAllBytes(Paths.get("/proc/self/stat")), Charset.defaultCharset());
        String pid = contentPid.split(" ")[0];
        System.setProperty("galeb.pid", pid);
    }

    public String getValue() {
        return value;
    }

    private final String value;

    SystemEnvs(String env, Object def) {
        this.value = env != null ? env : String.valueOf(def);
    }
}
