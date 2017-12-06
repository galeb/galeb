package io.galeb.core.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.util.Set;

@Entity
public class Target extends AbstractEntity implements WithStatus {

    @ManyToMany
    private Set<Pool> pools;

    @OneToMany
    private Set<HealthStatus> healthStatus;

    private String name;

    @Transient
    private Status status = Status.UNKNOWN;

    public Set<Pool> getPools() {
        return pools;
    }

    public void setPools(Set<Pool> pools) {
        this.pools = pools;
    }

    public Set<HealthStatus> getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(Set<HealthStatus> healthStatus) {
        this.healthStatus = healthStatus;
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
