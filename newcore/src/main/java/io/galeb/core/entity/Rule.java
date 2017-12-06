package io.galeb.core.entity;

import java.util.Set;

public class Rule {

    private Set<RuleGroup> ruleGroups;
    private Set<Pool> pools;
    private Project project;

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
}
