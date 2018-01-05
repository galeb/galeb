package io.galeb.core.entity;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "rolegroup", uniqueConstraints = { @UniqueConstraint(name = "UK_rolegroup_name", columnNames = { "name" }) })
public class RoleGroup extends AbstractEntity  {

    public enum Role {
        ADMIN,

        ACCOUNT_SAVE,
        ACCOUNT_SAVE_ALL,
        ACCOUNT_DELETE,
        ACCOUNT_DELETE_ALL,
        ACCOUNT_VIEW,
        ACCOUNT_VIEW_ALL,
        ACCOUNT_ADMIN,

        BALANCEPOLICY_SAVE,
        BALANCEPOLICY_SAVE_ALL,
        BALANCEPOLICY_DELETE,
        BALANCEPOLICY_DELETE_ALL,
        BALANCEPOLICY_VIEW,
        BALANCEPOLICY_VIEW_ALL,
        BALANCEPOLICY_ADMIN,

        ENVIRONMENT_SAVE,
        ENVIRONMENT_SAVE_ALL,
        ENVIRONMENT_DELETE,
        ENVIRONMENT_DELETE_ALL,
        ENVIRONMENT_VIEW,
        ENVIRONMENT_VIEW_ALL,
        ENVIRONMENT_ADMIN,

        HEALTHCHECK_SAVE,
        HEALTHCHECK_SAVE_ALL,
        HEALTHCHECK_DELETE,
        HEALTHCHECK_DELETE_ALL,
        HEALTHCHECK_VIEW,
        HEALTHCHECK_VIEW_ALL,
        HEALTHCHECK_ADMIN,

        HEALTHSTATUS_SAVE,
        HEALTHSTATUS_SAVE_ALL,
        HEALTHSTATUS_DELETE,
        HEALTHSTATUS_DELETE_ALL,
        HEALTHSTATUS_VIEW,
        HEALTHSTATUS_VIEW_ALL,
        HEALTHSTATUS_ADMIN,

        POOL_SAVE,
        POOL_SAVE_ALL,
        POOL_DELETE,
        POOL_DELETE_ALL,
        POOL_VIEW,
        POOL_VIEW_ALL,
        POOL_ADMIN,

        PROJECT_SAVE,
        PROJECT_SAVE_ALL,
        PROJECT_DELETE,
        PROJECT_DELETE_ALL,
        PROJECT_VIEW,
        PROJECT_VIEW_ALL,
        PROJECT_ADMIN,

        ROLEGROUP_SAVE,
        ROLEGROUP_SAVE_ALL,
        ROLEGROUP_DELETE,
        ROLEGROUP_DELETE_ALL,
        ROLEGROUP_VIEW,
        ROLEGROUP_VIEW_ALL,
        ROLEGROUP_ADMIN,

        RULE_SAVE,
        RULE_SAVE_ALL,
        RULE_DELETE,
        RULE_DELETE_ALL,
        RULE_VIEW,
        RULE_VIEW_ALL,
        RULE_ADMIN,

        RULEORDERED_SAVE,
        RULEORDERED_SAVE_ALL,
        RULEORDERED_DELETE,
        RULEORDERED_DELETE_ALL,
        RULEORDERED_VIEW,
        RULEORDERED_VIEW_ALL,
        RULEORDERED_ADMIN,

        TARGET_SAVE,
        TARGET_SAVE_ALL,
        TARGET_DELETE,
        TARGET_DELETE_ALL,
        TARGET_VIEW,
        TARGET_VIEW_ALL,
        TARGET_ADMIN,

        TEAM_SAVE,
        TEAM_SAVE_ALL,
        TEAM_DELETE,
        TEAM_DELETE_ALL,
        TEAM_VIEW,
        TEAM_VIEW_ALL,
        TEAM_ADMIN,

        VIRTUALHOST_SAVE,
        VIRTUALHOST_SAVE_ALL,
        VIRTUALHOST_DELETE,
        VIRTUALHOST_DELETE_ALL,
        VIRTUALHOST_VIEW,
        VIRTUALHOST_VIEW_ALL,
        VIRTUALHOST_ADMIN,

        VIRTUALHOSTGROUP_SAVE,
        VIRTUALHOSTGROUP_SAVE_ALL,
        VIRTUALHOSTGROUP_DELETE,
        VIRTUALHOSTGROUP_DELETE_ALL,
        VIRTUALHOSTGROUP_VIEW,
        VIRTUALHOSTGROUP_VIEW_ALL,
        VIRTUALHOSTGROUP_ADMIN
    }

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
