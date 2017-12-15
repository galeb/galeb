package io.galeb.core.entity;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "balancepolicy", uniqueConstraints = { @UniqueConstraint(name = "UK_balancepolicy_name", columnNames = { "name" }) })
public class BalancePolicy extends AbstractEntity {

    @OneToMany(mappedBy = "balancePolicy")
    private Set<Pool> pools = new HashSet<>();

    @Column(nullable = false)
    private String name;

    public Set<Pool> getPools() {
        return pools;
    }

    public void setPools(Set<Pool> pools) {
        if (pools != null) {
            this.pools.clear();
            this.pools.addAll(pools);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BalancePolicy that = (BalancePolicy) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getName());
    }
}
