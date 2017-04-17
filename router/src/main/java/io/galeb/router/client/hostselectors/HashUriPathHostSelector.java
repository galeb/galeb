package io.galeb.router.client.hostselectors;

import io.galeb.router.client.ExtendedLoadBalancingProxyClient.Host;
import io.galeb.router.SystemEnvs;
import io.galeb.router.client.hostselectors.consistenthash.ConsistentHash;
import io.galeb.router.client.hostselectors.consistenthash.HashAlgorithm;
import io.undertow.server.HttpServerExchange;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class HashUriPathHostSelector extends ClientStatisticsMarker implements HashHostSelector {

    private final HashAlgorithm hashAlgorithm = new HashAlgorithm(HashAlgorithm.HashType.valueOf(SystemEnvs.HASH_ALGORITHM.getValue()));
    private final int numReplicas = Integer.parseInt(SystemEnvs.HASH_NUM_REPLICAS.getValue());
    private final ConsistentHash<Integer> consistentHash = new ConsistentHash<>(hashAlgorithm, numReplicas, Collections.emptyList());
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    @Override
    public int selectHost(final Host[] availableHosts, final HttpServerExchange exchange) {
        if (!initialized.getAndSet(true)) {
            final LinkedHashSet<Integer> listPos = convertToMapStream(availableHosts)
                                                    .map(Map.Entry::getKey)
                                                    .collect(Collectors.toCollection(LinkedHashSet::new));
            consistentHash.rebuild(hashAlgorithm, numReplicas, listPos);
        }
        int pos = consistentHash.get(getKey(exchange));
        stamp(availableHosts[pos], exchange);
        return pos;
    }

    private String getKey(final HttpServerExchange exchange) {
        return exchange.getRelativePath();
    }

    // Test only
    @Override
    public synchronized void reset() {
        initialized.set(false);
    }
}
