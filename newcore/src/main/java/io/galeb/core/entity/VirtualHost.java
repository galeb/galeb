package io.galeb.core.entity;

import io.galeb.core.exceptions.BadRequestException;
import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "virtualhost", uniqueConstraints = { @UniqueConstraint(name = "UK_virtualhost_name", columnNames = { "name" }) })
public class VirtualHost extends AbstractEntity implements WithStatus {

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name="FK_virtualhost_project"))
    private Project project;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "rulegroup_id", nullable = false, foreignKey = @ForeignKey(name="FK_virtualhost_rulegroup"))
    private RuleGroup rulegroup;

    @ManyToMany(mappedBy = "virtualhosts")
    private Set<Environment> environments = new HashSet<>();

    @Column(nullable = false)
    private String name;

    @Transient
    private Status status = Status.UNKNOWN;

    public Set<Environment> getEnvironments() {
        return environments;
    }

    public void setEnvironments(Set<Environment> environments) {
        if (environments == null || environments.isEmpty()) {
            throw new BadRequestException("Environment(s) undefined");
        }
        this.environments.clear();
        this.environments.addAll(environments);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Assert.hasText(name, "name is not valid");
        this.name = name;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public RuleGroup getRulegroup() {
        return rulegroup;
    }

    public void setRulegroup(RuleGroup rulegroup) {
        this.rulegroup = rulegroup;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirtualHost that = (VirtualHost) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}