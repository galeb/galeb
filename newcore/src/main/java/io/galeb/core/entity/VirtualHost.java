package io.galeb.core.entity;

import java.util.Set;

public class VirtualHost extends AbstractEntity {

    private RuleGroup ruleGroup;
    private Set<Environment> environments;

    public RuleGroup getRuleGroup() {
        return ruleGroup;
    }

    public void setRuleGroup(RuleGroup ruleGroup) {
        this.ruleGroup = ruleGroup;
    }

    public Set<Environment> getEnvironments() {
        return environments;
    }

    public void setEnvironments(Set<Environment> environments) {
        this.environments = environments;
    }
}