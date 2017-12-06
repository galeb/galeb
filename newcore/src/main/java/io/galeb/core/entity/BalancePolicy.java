package io.galeb.core.entity;

import java.util.Set;

public class BalancePolicy extends AbstractEntity {

    private Set<Pool> pools;
    private String name;

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
}
