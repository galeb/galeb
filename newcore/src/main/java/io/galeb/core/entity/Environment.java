package io.galeb.core.entity;

import java.util.Set;

public class Environment {

    private Set<VirtualHost> virtualHosts;
    private Set<Pool> pools;

    public Set<VirtualHost> getVirtualHosts() {
        return virtualHosts;
    }

    public void setVirtualHosts(Set<VirtualHost> virtualHosts) {
        this.virtualHosts = virtualHosts;
    }

    public Set<Pool> getPools() {
        return pools;
    }

    public void setPools(Set<Pool> pools) {
        this.pools = pools;
    }
}
