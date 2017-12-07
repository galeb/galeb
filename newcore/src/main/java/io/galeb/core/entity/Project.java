package io.galeb.core.entity;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_project_name", columnNames = { "name" }) })
public class Project extends AbstractEntity {

    @OneToMany
    private Set<Rule> rules = new HashSet<>();

    @OneToMany
    private Set<Pool> pools = new HashSet<>();

    @ManyToMany
    private Set<Team> teams = new HashSet<>();

    @OneToMany
    private Set<VirtualHost> virtualHosts = new HashSet<>();

    @Column(nullable = false)
    private String name;

    public Set<Rule> getRules() {
        return rules;
    }

    public void setRules(Set<Rule> rules) {
        if (rules != null) {
            this.rules.clear();
            this.rules.addAll(rules);
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

    public Set<Team> getTeams() {
        return teams;
    }

    public void setTeams(Set<Team> teams) {
        if (teams != null) {
            this.teams.clear();
            this.teams.addAll(teams);
        }
    }

    public Set<VirtualHost> getVirtualHosts() {
        return virtualHosts;
    }

    public void setVirtualHosts(Set<VirtualHost> virtualHosts) {
        if (virtualHosts != null) {
            this.virtualHosts.clear();
            this.virtualHosts.addAll(virtualHosts);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Assert.hasText(name, "name is not valid");
        this.name = name;
    }

}
