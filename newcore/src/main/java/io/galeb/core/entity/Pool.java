package io.galeb.core.entity;

import java.util.Set;

public class Pool extends AbstractEntity {

    private Set<Rule> rules;
    private Set<Environment> environments;
    private Set<Target> targets;
    private Project project;
    private BalancePolicy balancePolicy;

    public Set<Rule> getRules() {
        return rules;
    }

    public void setRules(Set<Rule> rules) {
        this.rules = rules;
    }

    public Set<Environment> getEnvironments() {
        return environments;
    }

    public void setEnvironments(Set<Environment> environments) {
        this.environments = environments;
    }

    public Set<Target> getTargets() {
        return targets;
    }

    public void setTargets(Set<Target> targets) {
        this.targets = targets;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public BalancePolicy getBalancePolicy() {
        return balancePolicy;
    }

    public void setBalancePolicy(BalancePolicy balancePolicy) {
        this.balancePolicy = balancePolicy;
    }
}
