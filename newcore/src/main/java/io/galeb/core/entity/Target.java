package io.galeb.core.entity;

import io.galeb.core.exceptions.BadRequestException;
import javassist.tools.web.BadHttpRequest;
import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_target_name", columnNames = { "name" }) })
public class Target extends AbstractEntity implements WithStatus {

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JoinTable(joinColumns=@JoinColumn(name = "target_id", foreignKey = @ForeignKey(name="FK_pool_target_id")),
               inverseJoinColumns=@JoinColumn(name = "pool_id", nullable = false, foreignKey = @ForeignKey(name="FK_target_pool_id")))
    private Set<Pool> pools = new HashSet<>();

    @OneToMany(mappedBy = "target")
    private Set<HealthStatus> healthStatus = new HashSet<>();

    @Column(nullable = false)
    private String name;

    @Transient
    private Status status = Status.UNKNOWN;

    public Set<Pool> getPools() {
        return pools;
    }

    public void setPools(Set<Pool> pools) {
        if (pools == null || pools.isEmpty()) {
            throw new BadRequestException("Pool(s) undefined");
        }
        this.pools.clear();
        this.pools.addAll(pools);
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

    @Override
    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Target target = (Target) o;
        return Objects.equals(getName(), target.getName()) || Objects.equals(getId(), target.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
