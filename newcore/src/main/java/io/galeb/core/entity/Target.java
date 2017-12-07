package io.galeb.core.entity;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_name_target", columnNames = { "name" }) })
public class Target extends AbstractEntity implements WithStatus {

    @ManyToMany
    private Set<Pool> pools = new HashSet<>();

    @OneToMany
    private Set<HealthStatus> healthStatus = new HashSet<>();

    @Column(nullable = false)
    private String name;

    @Transient
    private Status status = Status.UNKNOWN;

    public Set<Pool> getPools() {
        return pools;
    }

    public void setPools(Set<Pool> pools) {
        if (pools != null) {
            this.pools.clear();
            this.pools.addAll(pools);
        }
    }

    public Set<HealthStatus> getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(Set<HealthStatus> healthStatus) {
        if (healthStatus != null) {
            this.healthStatus.clear();
            this.healthStatus.addAll(healthStatus);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Assert.hasText(name, "name is not valid");
        this.name = name;
    }

    @Override
    public Status getStatus() {
        return status;
    }
}
