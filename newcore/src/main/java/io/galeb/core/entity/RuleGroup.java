package io.galeb.core.entity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rulegroup")
public class RuleGroup extends AbstractEntity implements WithStatus {

    @OneToMany(mappedBy = "rulegroup")
    public Set<VirtualHost> virtualhosts = new HashSet<>();

    @OneToMany(mappedBy = "rulegroup")
    public Set<RuleOrdered> rulesOrdered;

    @Transient
    private Status status = Status.UNKNOWN;

    public Set<VirtualHost> getVirtualhosts() {
        return virtualhosts;
    }

    public void setVirtualhosts(Set<VirtualHost> virtualhosts) {
        if (virtualhosts != null) {
            this.virtualhosts.clear();
            this.virtualhosts.addAll(virtualhosts);
        }
    }

    public Set<RuleOrdered> getRulesOrdered() {
        return rulesOrdered;
    }

    public void setRulesOrdered(Set<RuleOrdered> rulesOrdered) {
        if (rulesOrdered != null) {
            this.rulesOrdered.clear();
            this.rulesOrdered.addAll(rulesOrdered);
        }
    }

    @Override
    public Status getStatus() {
        return status;
    }

}
