package io.galeb.core.entity;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "virtualhost", uniqueConstraints = { @UniqueConstraint(name = "UK_name_virtualhost", columnNames = { "name" }) })
public class VirtualHost extends AbstractEntity implements WithStatus {

    @ManyToOne
    private Project project;

    @ManyToOne
    private RuleGroup ruleGroup;

    @ManyToMany
    private Set<Environment> environments = new HashSet<>();

    @Column(nullable = false)
    private String name;

    @Column
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
        if (environments != null) {
            this.environments.clear();
            this.environments.addAll(environments);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Assert.hasText(name, "name is not valid");
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