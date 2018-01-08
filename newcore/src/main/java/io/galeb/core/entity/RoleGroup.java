package io.galeb.core.entity;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "rolegroup", uniqueConstraints = { @UniqueConstraint(name = "UK_rolegroup_name", columnNames = { "name" }) })
public class RoleGroup extends AbstractEntity  {

    @Column(nullable = false)
    private String name;

    @ElementCollection(targetClass = Role.class)
    @JoinTable(name = "rolegroup_roles",
            joinColumns = @JoinColumn(name = "rolegroup_id", nullable = false, foreignKey = @ForeignKey(name = "FK_rolegroup_role_id")))
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    @ManyToMany
    @JoinTable(joinColumns = @JoinColumn(name = "rolegroup_id", foreignKey = @ForeignKey(name = "FK_rolegroup_account_id")),
            inverseJoinColumns = @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "FK_account_rolegroup_id")))
    public Set<Account> accounts;

    @ManyToMany
    @JoinTable(joinColumns = @JoinColumn(name = "rolegroup_id", foreignKey = @ForeignKey(name = "FK_rolegroup_project_id")),
            inverseJoinColumns = @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name = "FK_project_rolegroup_id")))
    public Set<Account> projects;

    @ManyToMany
    @JoinTable(joinColumns = @JoinColumn(name = "rolegroup_id", foreignKey = @ForeignKey(name = "FK_rolegroup_team_id")),
            inverseJoinColumns = @JoinColumn(name = "team_id", nullable = false, foreignKey = @ForeignKey(name = "FK_account_team_id")))
    public Set<Account> teams;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Assert.hasText(name, "name is not valid");
        this.name = name;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        if (roles != null) {
            this.roles.clear();
            this.roles.addAll(roles);
        }
    }

    public Set<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<Account> accounts) {
        if (accounts != null) {
            this.accounts.clear();
            this.accounts.addAll(accounts);
        }
    }

    public Set<Account> getProjects() {
        return projects;
    }

    public void setProjects(Set<Account> projects) {
        if (projects != null) {
            this.projects.clear();
            this.projects.addAll(projects);
        }
    }

    public Set<Account> getTeams() {
        return teams;
    }

    public void setTeams(Set<Account> teams) {
        if (teams != null) {
            this.teams.clear();
            this.teams.addAll(teams);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleGroup roleGroup = (RoleGroup) o;
        return Objects.equals(getName(), roleGroup.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
