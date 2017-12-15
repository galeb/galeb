package io.galeb.core.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @ManyToMany(mappedBy = "virtualhosts", cascade = CascadeType.REMOVE)
    private Set<RuleOrdered> rulesOrdered = new HashSet<>();

    @ManyToMany(mappedBy = "virtualhosts")
    private Set<Environment> environments = new HashSet<>();

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "link_id", foreignKey = @ForeignKey(name="FK_virtualhost_link"))
    private VirtualHost link;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "link")
    private Set<VirtualHost> aliases = new HashSet<>();

    @Transient
    private Status status = Status.UNKNOWN;

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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Set<RuleOrdered> getRulesOrdered() {
        return rulesOrdered;
    }

    public void setRulesOrdered(Set<RuleOrdered> rulesOrdered) {
        this.rulesOrdered = rulesOrdered;
    }

    public VirtualHost getLink() {
        return link;
    }

    public void setLink(VirtualHost link) {
        if (link != null) {
            if (link.getName().equalsIgnoreCase(getName()) || link.getId() == getId()) {
                throw new IllegalArgumentException("Self link NOT ALLOWED");
            }
            if (getRulesOrdered().size() > 0) {
                throw new IllegalArgumentException("Change Virtualhost (principal) to Virtualhost Alias NOT ALLOWED");
            }
        }
        this.link = link;
    }

    public Set<VirtualHost> getAliases() {
        return aliases;
    }

    public void setAliases(Set<VirtualHost> aliases) {
        if (aliases != null) {
            this.aliases.clear();
            this.aliases.addAll(aliases);
        }
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