package io.galeb.core.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Set;

@Entity
public class BalancePolicy extends AbstractEntity {

    @OneToMany
    private Set<Pool> pools;

    @Column(name = "name", nullable = false)
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
