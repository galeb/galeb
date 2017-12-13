package io.galeb.core.entity;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rulegroup", uniqueConstraints = { @UniqueConstraint(name = "UK_rulegroup_name", columnNames = { "name" }) })
public class RuleGroup extends AbstractEntity implements WithStatus {

    @OneToMany(mappedBy = "rulegroup")
    private Set<VirtualHost> virtualhosts = new HashSet<>();

    @OneToMany(mappedBy = "rulegroup")
    private Set<RuleOrdered> rulesOrdered = new HashSet<>();

    @Column(nullable = false)
    private String name;

    @Transient
    private Status status = Status.UNKNOWN;

    public Set<VirtualHost> getVirtualhosts() {
        return virtualhosts;
    }

    public void setVirtualhosts(Set<VirtualHost> virtualhosts) {
        if (virtualhosts != null) {
            this.virtualhosts.clear();
            this.virtualhosts.addAll(virtualhosts);
        }
    }

    public Set<RuleOrdered> getRulesOrdered() {
        return rulesOrdered;
    }

    public void setRulesOrdered(Set<RuleOrdered> rulesOrdered) {
        if (rulesOrdered != null) {
            this.rulesOrdered.clear();
            this.rulesOrdered.addAll(rulesOrdered);
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
