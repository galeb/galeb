package io.galeb.health;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
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
    ROUTER_PORT           (System.getenv("HEALTH_PORT"),           7000),

    /**
     * Syslog server host.
     */
    SYSLOG_HOST           (System.getenv("SYSLOG_HOST"),           "127.0.0.1"),

    /**
     * Syslog server port
     */
    SYSLOG_PORT           (System.getenv("SYSLOG_PORT"),           514),

    /**
     *  AMQP Queue name
     */
    QUEUE_NAME(System.getenv("QUEUE_NAME"), "galeb-health"),

    /**
     *  Galeb Manager Farm -> Environment ID
     */
    ENVIRONMENT_ID(System.getenv("ENVIRONMENT_ID"), 2),

    /**
     *  Galeb Manager URL
     */
    MANAGER_URL(System.getenv("MANAGER_URL"), "http://127.0.0.1:8000"),

    /**
     *  Galeb Manager user
     */
    MANAGER_USER(System.getenv("MANAGER_USER"), "health"),

    /**
     *  Galeb Manager password
     */
    MANAGER_PASS(System.getenv("MANAGER_PASS"), "password"),

    /**
     *  Tester request timeout
     */
    TEST_TIMEOUT(System.getenv("TEST_TIMEOUT"), 2000);

    static {
        try {
            String contentPid = new String(Files.readAllBytes(Paths.get("/proc/self/stat")), Charset.defaultCharset());
            String pid = contentPid.split(" ")[0];
            System.setProperty("galeb.pid", pid);
        } catch (IOException ignore) {
            //
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
