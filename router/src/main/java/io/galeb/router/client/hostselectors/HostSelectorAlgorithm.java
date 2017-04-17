/**
 *
 */

package io.galeb.router.client.hostselectors;

@SuppressWarnings("unused")
public enum HostSelectorAlgorithm {
    ROUNDROBIN       (RoundRobinHostSelector.class),
    STRICT_LEASTCONN (LeastConnHostSelector.class),
    LEASTCONN        (LeastConnWithRRHostSelector.class),
    HASH_SOURCEIP    (HashSourceIpHostSelector.class),
    HASH_URIPATH     (HashUriPathHostSelector.class);

    private final Class klazz;
    HostSelectorAlgorithm(final Class klazz) {
        this.klazz = klazz;
    }

    public HostSelector getHostSelector() throws IllegalAccessException, InstantiationException {
        return (HostSelector) klazz.newInstance();
    }
}
