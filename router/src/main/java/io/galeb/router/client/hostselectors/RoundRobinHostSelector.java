/**
 *
 */

package io.galeb.router.client.hostselectors;

import io.galeb.router.client.ExtendedLoadBalancingProxyClient.Host;
import io.undertow.server.HttpServerExchange;

import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinHostSelector extends ClientStatisticsMarker implements HostSelector {

    private volatile int currentHost = 0;

    @Override
    public int selectHost(final Host[] availableHosts, final HttpServerExchange exchange) {
        int pos = currentHost;
        currentHost = ++currentHost % availableHosts.length;
        stamp(availableHosts[pos], exchange);
        return pos;
    }

    // Test only
    public synchronized void reset() {
        currentHost = 0;
    }
}
