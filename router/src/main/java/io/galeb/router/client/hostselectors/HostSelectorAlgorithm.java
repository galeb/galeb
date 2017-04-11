/**
 *
 */

package io.galeb.router.client.hostselectors;

public enum HostSelectorAlgorithm {
    ROUNDROBIN (new RoundRobinHostSelector()),
    LEASTCONN (new LeastConnHostSelector()),
    HASH_SOURCEIP (new HashSourceIpHostSelector());

    private final HostSelector hostSelector;
    public HostSelector getHostSelector() { return hostSelector; }
    HostSelectorAlgorithm(final HostSelector hostSelector) {
        this.hostSelector = hostSelector;
    }
}
