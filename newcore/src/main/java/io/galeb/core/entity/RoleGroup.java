package io.galeb.core.entity;

import org.springframework.util.Assert;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Objects;

@Entity
@Table(name = "rolegroup", uniqueConstraints = { @UniqueConstraint(name = "UK_rolegroup_name", columnNames = { "name" }) })
public class RoleGroup extends AbstractEntity  {

    @Column(nullable = false)
    private String name;

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
        RoleGroup roleGroup = (RoleGroup) o;
        return Objects.equals(getName(), roleGroup.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
