package io.galeb.core.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;
import java.util.Set;

@Entity
public class Environment extends AbstractEntity implements WithStatus {

    @ManyToMany
    private Set<VirtualHost> virtualHosts;

    @ManyToMany
    private Set<Pool> pools;

    private String name;

    @Transient
    private Status status = Status.UNKNOWN;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Status getStatus() {
        return status;
    }
}

