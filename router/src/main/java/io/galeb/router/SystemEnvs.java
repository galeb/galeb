package io.galeb.router;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static io.galeb.router.consistenthash.HashAlgorithm.HashType.MURMUR3_32;

public enum SystemEnvs {

    CLUSTER_ID(System.getenv("CLUSTER_ID"), "GALEB"),
    ROUTER_PORT(System.getenv("ROUTER_PORT"), 8000),
    ETCD_SERVER(System.getenv("ETCD_SERVER"), "http://127.0.0.1:2379"),
    STATSD_PREFIX(System.getenv("STATSD_PREFIX"), CLUSTER_ID.getValue()),
    STATSD_HOST(System.getenv("STATSD_HOST"), "127.0.0.1"),
    STATSD_PORT(System.getenv("STATSD_PORT"), 8125),
    IO_THREADS(System.getenv("IO_THREADS"), Math.max(Runtime.getRuntime().availableProcessors(), 2)),
    WORKER_THREADS(System.getenv("WORKER_THREADS"), Integer.parseInt(IO_THREADS.getValue()) * 8),
    BUFFER_SIZE(System.getenv("BUFFER_SIZE"), 16384),
    DIRECT_BUFFER(System.getenv("DIRECT_BUFFER"), Boolean.TRUE),
    BACKLOG(System.getenv("BACKLOG"), getSOBacklog()),
    POOL_CONN_TTL(System.getenv("POOL_CONN_TTL"), Math.toIntExact(TimeUnit.HOURS.toMillis(1))),
    POOL_SOFTMAXCONN(System.getenv("POOL_SOFTMAXCONN"), 2000),
    POOL_CONN_PER_THREAD(System.getenv("POOL_CONN_PER_THREAD"), 2000),
    POOL_MAX_REQUESTS(System.getenv("POOL_MAX_REQUESTS"), -1),
    ENABLE_ACCESSLOG(System.getenv("ENABLE_ACCESSLOG"), Boolean.TRUE),
    ENABLE_STATSD(System.getenv("ENABLE_STATSD"), Boolean.TRUE),
    REUSE_XFORWARDED(System.getenv("REUSE_XFORWARDED"), Boolean.TRUE),
    REWRITE_HOST_HEADER(System.getenv("REWRITE_HOST_HEADER"), Boolean.FALSE),
    IGNORE_XFORWARDED_FOR(System.getenv("IGNORE_XFORWARDED_FOR"), Boolean.FALSE),
    HASH_NUM_REPLICAS(System.getenv("HASH_NUM_REPLICAS"), 100),
    HASH_ALGORITHM(System.getenv("HASH_ALGORITHM"), MURMUR3_32);

    private static Integer getSOBacklog() {
        try {
            final Path tcp_max_syn_backlog_file = Paths.get("/proc/sys/net/ipv4/tcp_max_syn_backlog");
            String tcp_max_syn_backlog = new String(Files.readAllBytes(tcp_max_syn_backlog_file), Charset.defaultCharset());
            return Integer.valueOf(tcp_max_syn_backlog);
        } catch (IOException e) {
            return 1000;
        }
    }

    public String getValue() {
        return value;
    }

    private final String value;

    SystemEnvs(String env, Object def) {
        this.value = env != null ? env : String.valueOf(def);
    }
}
