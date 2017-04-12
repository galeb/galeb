/**
 *
 */

package io.galeb.router.client.hostselectors;

@SuppressWarnings("unused")
public enum HostSelectorAlgorithm {
    ROUNDROBIN (new RoundRobinHostSelector()),
    STRICT_LEASTCONN (new LeastConnHostSelector()),
    LEASTCONN (new LeastConnWithRRHostSelector()),
    HASH_SOURCEIP (new HashSourceIpHostSelector()),
    HASH_URIPATH (new HashUriPathHostSelector());

    private final HostSelector hostSelector;
    public HostSelector getHostSelector() { return hostSelector; }
    HostSelectorAlgorithm(final HostSelector hostSelector) {
        this.hostSelector = hostSelector;
    }
}
