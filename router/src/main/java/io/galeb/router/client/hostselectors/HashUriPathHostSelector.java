package io.galeb.router.client.hostselectors;

import io.galeb.router.client.ExtendedLoadBalancingProxyClient;
import io.galeb.router.consistenthash.ConsistentHash;
import io.galeb.router.consistenthash.HashAlgorithm;
import io.undertow.server.HttpServerExchange;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static io.galeb.router.consistenthash.HashAlgorithm.HashType.SIP24;

public class HashUriPathHostSelector implements HostSelector {

    private static final int NUM_REPLICAS = 1;

    private final HashAlgorithm hashAlgorithm = new HashAlgorithm(SIP24);
    private final ConsistentHash<Integer> consistentHash = new ConsistentHash<>(hashAlgorithm, NUM_REPLICAS, Collections.emptyList());
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    @Override
    public int selectHost(final ExtendedLoadBalancingProxyClient.Host[] availableHosts, final HttpServerExchange exchange) {
        if (!initialized.getAndSet(true)) {
            final LinkedHashSet<Integer> listPos = convertToMapStream(availableHosts)
                                                    .map(Map.Entry::getKey)
                                                    .collect(Collectors.toCollection(LinkedHashSet::new));
            consistentHash.rebuild(hashAlgorithm, NUM_REPLICAS, listPos);
        }
        return consistentHash.get(getKey(exchange));
    }

    private String getKey(final HttpServerExchange exchange) {
        return exchange.getRelativePath();
    }
}
