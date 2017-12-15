package io.galeb.core.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "ruleordered")
public class RuleOrdered extends AbstractEntity implements Comparable<RuleOrdered> {

    private static final long serialVersionUID = 1L;

    @ManyToMany
    @JoinTable(joinColumns=@JoinColumn(name = "rule_order_id", foreignKey = @ForeignKey(name="FK_virtualhost_rule_order_id")),
            inverseJoinColumns=@JoinColumn(name = "virtualhost_id", foreignKey = @ForeignKey(name="FK_rule_order_virtualhost_id")))
    private Set<VirtualHost> virtualhosts = new HashSet<>();

    @JsonProperty("order")
    @Column(name = "rule_order", nullable = false)
    private Integer order = Integer.MAX_VALUE;

    @ManyToOne
    @JoinColumn(name = "rule_rule_ordered_id", nullable = false, foreignKey = @ForeignKey(name="FK_rule_rule_ordered"))
    private Rule rule;

    public Set<VirtualHost> getVirtualhosts() {
        return virtualhosts;
    }

    public void setVirtualhosts(Set<VirtualHost> virtualhosts) {
        if (virtualhosts != null) {
            if (virtualhosts.stream().anyMatch(v -> v.getPrincipal() != null)) throw new IllegalArgumentException("Using RuleOrdered with Virtualhost alias (not principal) NOT ALLOWED. Use the PRINCIPAL Virtualhost");
            this.virtualhosts.clear();
            this.virtualhosts.addAll(virtualhosts);
        }
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
