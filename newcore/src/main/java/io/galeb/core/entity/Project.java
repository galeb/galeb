package io.galeb.core.entity;

import java.util.Set;

public class Project extends AbstractEntity {

    private Set<Rule> rules;
    private Set<RuleGroup> ruleGroups;
    private Set<Pool> pools;
    private Set<Team> teams;

    public Set<Rule> getRules() {
        return rules;
    }

    public void setRules(Set<Rule> rules) {
        this.rules = rules;
    }

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

    public Set<Team> getTeams() {
        return teams;
    }

    public void setTeams(Set<Team> teams) {
        this.teams = teams;
    }
}
