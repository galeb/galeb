/**
 *
 */

package io.galeb.router.client.hostselectors;

import io.galeb.router.client.ExtendedLoadBalancingProxyClient.Host;
import io.undertow.server.HttpServerExchange;

import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinHostSelector extends ClientStatisticsMarker implements HostSelector {

    private final AtomicInteger currentHost = new AtomicInteger(0);

    @Override
    public int selectHost(final Host[] availableHosts, final HttpServerExchange exchange) {
        final int pos = currentHost.incrementAndGet() % availableHosts.length;
        stamp(availableHosts[pos], exchange);
        return pos;
    }

    // Test only
    public synchronized void reset() {
        currentHost.set(0);
    }
}
