package io.galeb.core.entity;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
public class RuleGroup implements WithStatus {

    @ManyToMany
    private Set<VirtualHost> virtualHosts = new HashSet<>();

    @ElementCollection
    @JoinColumn(nullable = false)
    private Map<Integer, Rule> rules = new HashMap<>();

    @Column(nullable = false)
    private String name;

    @Transient
    private Status status = Status.UNKNOWN;

    public Set<VirtualHost> getVirtualHosts() {
        return virtualHosts;
    }

    public void setVirtualHosts(Set<VirtualHost> virtualHosts) {
        if (virtualHosts != null) {
            this.virtualHosts.clear();
            this.virtualHosts.addAll(virtualHosts);
        }
    }

    public Map<Integer, Rule> getRules() {
        return rules;
    }

    public void setRules(Map<Integer, Rule> rules) {
        if (rules != null) {
            this.rules.clear();
            this.rules.putAll(rules);
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
