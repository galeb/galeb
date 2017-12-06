package io.galeb.core.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import java.util.Set;

@Entity
public class VirtualHost extends AbstractEntity implements WithStatus {

    @ManyToOne
    private Project project;

    @ManyToOne
    private RuleGroup ruleGroup;

    @ManyToMany
    private Set<Environment> environments;

    private String name;

    private String alias;

    @Transient
    private Status status = Status.UNKNOWN;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public Status getStatus() {
        return status;
    }
}