package io.galeb.core.entity;

import javax.persistence.*;
import java.util.Map;
import java.util.Set;

@Entity
public class RuleGroup implements WithStatus {

    @ManyToMany
    private Set<VirtualHost> virtualHosts;

    @ElementCollection
    @JoinColumn(nullable = false)
    private Map<Integer, Rule> rules;

    @Column(name = "name", nullable = false)
    private String name;

    @Transient
    private Status status = Status.UNKNOWN;

    public Set<VirtualHost> getVirtualHosts() {
        return virtualHosts;
    }

    public void setVirtualHosts(Set<VirtualHost> virtualHosts) {
        this.virtualHosts = virtualHosts;
    }

    public Map<Integer, Rule> getRules() {
        return rules;
    }

    public void setRules(Map<Integer, Rule> rules) {
        this.rules = rules;
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
