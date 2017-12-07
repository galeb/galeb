package io.galeb.core.entity;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Rule extends AbstractEntity implements WithStatus {

    @ManyToMany
    private Set<RuleGroup> ruleGroups = new HashSet<>();

    @ManyToMany
    private Set<Pool> pools = new HashSet<>();

    @ManyToOne
    private Project project;

    @Column(nullable = false)
    private String match;

    private Boolean global = false;

    @Column(nullable = false)
    private String name;

    @Transient
    private Status status = Status.UNKNOWN;

    public Set<RuleGroup> getRuleGroups() {
        return ruleGroups;
    }

    public void setRuleGroups(Set<RuleGroup> ruleGroups) {
        if (ruleGroups != null) {
            this.ruleGroups.clear();
            this.ruleGroups.addAll(ruleGroups);
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        Assert.notNull(project, "Project is NULL");
        this.project = project;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        Assert.hasText(match, "match is not valid");
        this.match = match;
    }

    public Boolean getGlobal() {
        return global;
    }

    public void setGlobal(Boolean global) {
        if (global != null) {
            this.global = global;
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
