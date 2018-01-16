package io.galeb.legba.model.v2;

import java.util.Set;

public class VirtualhostGroup {

    private Set<RuleOrdered> rulesordered;

    public Set<RuleOrdered> getRulesordered() {
        return rulesordered;
    }

    public void setRulesordered(Set<RuleOrdered> rulesordered) {
        this.rulesordered = rulesordered;
    }
}
