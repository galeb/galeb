/**
 *
 */

package io.galeb.router.client.hostselectors;

import io.galeb.router.client.ExtendedLoadBalancingProxyClient;
import io.undertow.server.HttpServerExchange;

public class HostSelectorInitializer implements HostSelector {

    private HostSelector hostSelector = new RoundRobinHostSelector();

    public HostSelectorInitializer setHostSelector(final HostSelector hostSelector) {
        this.hostSelector = hostSelector;
        return this;
    }

    public HostSelector getHostSelector() {
        return hostSelector;
    }

    @Override
    public int selectHost(final ExtendedLoadBalancingProxyClient.Host[] availableHosts, final HttpServerExchange exchange) {
        return hostSelector.selectHost(availableHosts, exchange);
    }
}
