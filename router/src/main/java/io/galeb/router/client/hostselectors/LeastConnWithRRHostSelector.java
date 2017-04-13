/**
 *
 */

package io.galeb.router.client.hostselectors;

import io.galeb.router.client.ExtendedLoadBalancingProxyClient;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class LeastConnWithRRHostSelector extends ClientStatisticsMarker implements HostSelector {

    @SuppressWarnings("WeakerAccess")
    public static final AttachmentKey<Float> CUTTING_LINE_ATTACH = AttachmentKey.create(Float.class);

    private ConcurrentLinkedQueue<Integer> leastConnList = null;

    private float cuttingLine = 0.666f;

    @Override
    public int selectHost(final ExtendedLoadBalancingProxyClient.Host[] availableHosts, final HttpServerExchange exchange) {
        if (leastConnList == null || leastConnList.isEmpty()) {
            Float tempCuttingLine = exchange.getAttachment(CUTTING_LINE_ATTACH);
            cuttingLine = tempCuttingLine != null ? tempCuttingLine : cuttingLine;

            leastConnList = convertToMapStream(availableHosts)
                            .sorted(Comparator.comparing(e -> e.getValue().getOpenConnection()))
                            .limit(Integer.toUnsignedLong((int) ((availableHosts.length * cuttingLine) - Float.MIN_VALUE)))
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
        }
        int pos = leastConnList.poll();
        stamp(availableHosts[pos], exchange);
        return pos;
    }
}
