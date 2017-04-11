package io.galeb.router.client.hostselectors;

import io.galeb.router.client.ExtendedLoadBalancingProxyClient;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ClientStatisticsMarker {

    public static final AttachmentKey<Integer> TARGET_CONN = AttachmentKey.create(Integer.class);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected void stamp(final ExtendedLoadBalancingProxyClient.Host host, final HttpServerExchange exchange) {
        final int openConnections = host.getOpenConnection();
        exchange.putAttachment(TARGET_CONN, openConnections);
        if (logger.isDebugEnabled()) {
            final String uri = host.getUri().toString();
            logger.debug("{\"client_statistic\": { \"uri\": \"" + uri + "\", \"open_connections\": " + openConnections + "} }");
        }
    }
}
