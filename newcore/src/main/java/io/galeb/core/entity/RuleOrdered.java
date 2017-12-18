package io.galeb.core.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "ruleordered")
public class RuleOrdered extends AbstractEntity implements Comparable<RuleOrdered> {

    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "rulegroup_rule_ordered_id", nullable = false, foreignKey = @ForeignKey(name="FK_rulegroup_rule_ordered"))
    private RuleGroup rulegroup;

    @JsonProperty("order")
    @Column(name = "rule_order", nullable = false)
    private Integer order = Integer.MAX_VALUE;

    @ManyToOne
    @JoinColumn(name = "rule_rule_ordered_id", nullable = false, foreignKey = @ForeignKey(name="FK_rule_rule_ordered"))
    private Rule rule;

    public RuleGroup getRulegroup() {
        return rulegroup;
    }

    public void setRulegroup(RuleGroup rulegroup) {
        this.rulegroup = rulegroup;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleOrdered that = (RuleOrdered) o;
        return Objects.equals(rule, that.rule);
    }

    @Override
    public int hashCode() {

        return Objects.hash(rule);
    }

    @Override
    public int compareTo(RuleOrdered o) {
        return getRule().equals(o.getRule()) ? 0 : getOrder().compareTo(o.getOrder());
    }
}
