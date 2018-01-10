package io.galeb.core.entity;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_team_name", columnNames = { "name" }) })
public class Team extends AbstractEntity {

    @ManyToMany
    @JoinTable(joinColumns=@JoinColumn(name = "team_id", nullable = false, foreignKey = @ForeignKey(name="FK_team_id")),
            inverseJoinColumns=@JoinColumn(name = "account_id", foreignKey = @ForeignKey(name="FK_account_id")))
    private Set<Account> accounts = new HashSet<>();

    @ManyToMany(mappedBy = "teams")
    private Set<Project> projects = new HashSet<>();

    @Column(nullable = false)
    private String name;

    @ManyToMany(mappedBy = "teams", fetch = FetchType.EAGER)
    private Set<RoleGroup> rolegroups = new HashSet<>();

    public Set<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<Account> accounts) {
        if (accounts != null) {
            this.accounts.clear();
            this.accounts.addAll(accounts);
        }
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public void setProjects(Set<Project> projects) {
        if (projects != null) {
            this.projects.clear();
            this.projects.addAll(projects);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Assert.hasText(name, "name is not valid");
        this.name = name;
    }

    public Set<RoleGroup> getRolegroups() {
        return rolegroups;
    }

    public void setRolegroups(Set<RoleGroup> rolegroups) {
        if (rolegroups != null) {
            this.rolegroups.clear();
            this.rolegroups.addAll(rolegroups);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equals(getName(), team.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
