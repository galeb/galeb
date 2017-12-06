package io.galeb.core.entity;

import java.util.Set;

public class Target extends AbstractEntity {

    private Set<Pool> pools;
    private Set<Status> status;

    public Set<Pool> getPools() {
        return pools;
    }

    public void setPools(Set<Pool> pools) {
        this.pools = pools;
    }

    public Set<Status> getStatus() {
        return status;
    }

    public void setStatus(Set<Status> status) {
        this.status = status;
    }
}
