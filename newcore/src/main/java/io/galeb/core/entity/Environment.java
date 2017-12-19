package io.galeb.core.entity;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_environment_name", columnNames = { "name" }) })
public class Environment extends AbstractEntity implements WithStatus {

    @ManyToMany
    @JoinTable(joinColumns = @JoinColumn(name = "environment_id", nullable = false, foreignKey = @ForeignKey(name = "FK_environment_id")),
            inverseJoinColumns = @JoinColumn(name = "virtualhost_id", foreignKey = @ForeignKey(name = "FK_virtualhost_id")))
    private Set<VirtualHost> virtualhosts = new HashSet<>();

    @OneToMany(mappedBy = "environment")
    private Set<Pool> pools = new HashSet<>();

    @Column(nullable = false)
    private String name;

    @Transient
    private Status status = Status.UNKNOWN;

    public Set<VirtualHost> getVirtualHosts() {
        return virtualhosts;
    }

    public void setVirtualHosts(Set<VirtualHost> virtualHosts) {
        if (virtualHosts != null) {
            this.virtualhosts.clear();
            this.virtualhosts.addAll(virtualHosts);
        }
    }

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
        Environment that = (Environment) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getName());
    }
}

