package io.galeb.core.entity;

import java.util.Set;

public class Rule extends AbstractEntity implements WithStatus {

    private Set<RuleGroup> ruleGroups;
    private Set<Pool> pools;
    private Project project;
    private String match;
    private Boolean global;
    private String name;

    public Set<RuleGroup> getRuleGroups() {
        return ruleGroups;
    }

    public void setRuleGroups(Set<RuleGroup> ruleGroups) {
        this.ruleGroups = ruleGroups;
    }

    public Set<Pool> getPools() {
        return pools;
    }

    public void setPools(Set<Pool> pools) {
        this.pools = pools;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public Boolean getGlobal() {
        return global;
    }

    public void setGlobal(Boolean global) {
        this.global = global;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
