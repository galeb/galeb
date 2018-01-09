package io.galeb.core.entity;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@NamedQueries({
        @NamedQuery(
                name = "roleGroupsFromProject",
                query = "SELECT r FROM RoleGroup r INNER JOIN r.projects p INNER JOIN p.teams t INNER JOIN t.accounts a WHERE a.id = :account_id AND p.id = :project_id"),
        @NamedQuery(
                name = "roleGroupsFromTeams",
                query = "SELECT r FROM RoleGroup r INNER JOIN r.teams t INNER JOIN t.accounts a WHERE a.id = :id"),
        @NamedQuery(
                name = "roleGroupsFromAccount",
                query = "SELECT r FROM RoleGroup r INNER JOIN r.accounts a WHERE a.id = :id"),
        @NamedQuery(
                name = "roleGroupsTeam",
                query = "SELECT r FROM RoleGroup r INNER JOIN r.teams t INNER JOIN t.accounts a WHERE a.id = :account_id AND t.id = :team_id")
})

@Entity
@Table(name = "rolegroup", uniqueConstraints = { @UniqueConstraint(name = "UK_rolegroup_name", columnNames = { "name" }) })
public class RoleGroup extends AbstractEntity  {

    @Column(nullable = false)
    private String name;

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @JoinTable(name = "rolegroup_roles",
            joinColumns = @JoinColumn(name = "rolegroup_id", nullable = false, foreignKey = @ForeignKey(name = "FK_rolegroup_role_id")))
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    @ManyToMany(cascade = CascadeType.REFRESH)
    @JoinTable(joinColumns = @JoinColumn(name = "rolegroup_id", foreignKey = @ForeignKey(name = "FK_rolegroup_account_id")),
            inverseJoinColumns = @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "FK_account_rolegroup_id")))
    public Set<Account> accounts;

    @ManyToMany(cascade = CascadeType.REFRESH)
    @JoinTable(joinColumns = @JoinColumn(name = "rolegroup_id", foreignKey = @ForeignKey(name = "FK_rolegroup_project_id")),
            inverseJoinColumns = @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name = "FK_project_rolegroup_id")))
    public Set<Project> projects;

    @ManyToMany(cascade = CascadeType.REFRESH)
    @JoinTable(joinColumns = @JoinColumn(name = "rolegroup_id", foreignKey = @ForeignKey(name = "FK_rolegroup_team_id")),
            inverseJoinColumns = @JoinColumn(name = "team_id", nullable = false, foreignKey = @ForeignKey(name = "FK_account_team_id")))
    public Set<Team> teams;

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

    public Set<Project> getProjects() {
        return projects;
    }

    public void setProjects(Set<Project> projects) {
        if (projects != null) {
            this.projects.clear();
            this.projects.addAll(projects);
        }
    }

    public Set<Team> getTeams() {
        return teams;
    }

    public void setTeams(Set<Team> teams) {
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
