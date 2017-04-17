package io.galeb.router.client.hostselectors;

import io.galeb.router.SystemEnvs;
import io.galeb.router.client.ExtendedLoadBalancingProxyClient.Host;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ClientStatisticsMarker {

    public static final AttachmentKey<Integer> TARGET_CONN = AttachmentKey.create(Integer.class);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final boolean sendOpenconnCounter = Boolean.parseBoolean(SystemEnvs.SEND_OPENCONN_COUNTER.getValue());

    void stamp(final Host host, final HttpServerExchange exchange) {
        int openConnections = 0;
        if (sendOpenconnCounter) {
            openConnections = host.getOpenConnection();
            exchange.putAttachment(TARGET_CONN, openConnections);
        }
        if (logger.isDebugEnabled()) {
            final String uri = host.getUri().toString();
            logger.debug("{\"client_statistic\": { \"uri\": \"" + uri + "\", \"open_connections\": " + openConnections + "} }");
        }
    }
}
