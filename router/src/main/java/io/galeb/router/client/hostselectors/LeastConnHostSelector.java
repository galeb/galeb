/**
 *
 */

package io.galeb.router.client.hostselectors;

import io.galeb.router.client.ExtendedLoadBalancingProxyClient.Host;
import io.undertow.server.HttpServerExchange;

import java.util.Comparator;
import java.util.Map;

public class LeastConnHostSelector extends ClientStatisticsMarker implements HostSelector {

    @Override
    public int selectHost(final Host[] availableHosts, final HttpServerExchange exchange) {
        int pos = convertToMapStream(availableHosts)
                .sorted(Comparator.comparing(e -> e.getValue().getOpenConnection()))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(DEFAULT_POS);
        stamp(availableHosts[pos], exchange);
        return pos;
    }
}
