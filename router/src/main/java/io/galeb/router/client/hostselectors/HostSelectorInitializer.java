/**
 *
 */

package io.galeb.router.client.hostselectors;

import io.galeb.router.client.ExtendedLoadBalancingProxyClient;
import io.undertow.server.HttpServerExchange;

public class HostSelectorInitializer implements HostSelector {

    private HostSelector hostSelector = new LeastConnHostSelector();

    public HostSelectorInitializer setHostSelector(final HostSelector hostSelector) {
        this.hostSelector = hostSelector;
        return this;
    }

    @Override
    public int selectHost(final ExtendedLoadBalancingProxyClient.Host[] availableHosts, final HttpServerExchange exchange) {
        return hostSelector.selectHost(availableHosts, exchange);
    }
}
