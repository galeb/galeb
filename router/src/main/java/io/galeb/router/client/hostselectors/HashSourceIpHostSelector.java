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

public class HashSourceIpHostSelector extends ClientStatisticsMarker implements HashHostSelector {

    private final HashAlgorithm hashAlgorithm = new HashAlgorithm(HashAlgorithm.HashType.valueOf(SystemEnvs.HASH_ALGORITHM.getValue()));
    private final int numReplicas = Integer.parseInt(SystemEnvs.HASH_NUM_REPLICAS.getValue());
    private final ConsistentHash<Integer> consistentHash = new ConsistentHash<>(hashAlgorithm, numReplicas, Collections.emptyList());
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final boolean ignoreXForwardedFor = Boolean.parseBoolean(SystemEnvs.IGNORE_XFORWARDED_FOR.getValue());

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
        String aSourceIP;
        String defaultSourceIp = "127.0.0.1";
        String httpHeaderXrealIp = "X-Real-IP";
        String httpHeaderXForwardedFor = "X-Forwarded-For";

        if (exchange == null) {
            return defaultSourceIp;
        }

        if (ignoreXForwardedFor) {
            aSourceIP = exchange.getSourceAddress().getHostString();
        } else {
            aSourceIP = exchange.getRequestHeaders().getFirst(httpHeaderXrealIp);
            if (aSourceIP!=null) {
                return aSourceIP;
            }
            aSourceIP = exchange.getRequestHeaders().getFirst(httpHeaderXForwardedFor);
            if (aSourceIP!=null) {
                return aSourceIP.contains(",") ? aSourceIP.split(",")[0] : aSourceIP;
            }
            aSourceIP = exchange.getSourceAddress().getHostString();
        }

        return aSourceIP!=null ? aSourceIP : defaultSourceIp;
    }

    // Test only
    @Override
    public synchronized void reset() {
        initialized.set(false);
    }
}
