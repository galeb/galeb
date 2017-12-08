package io.galeb.core.entity;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_team_name", columnNames = { "name" }) })
public class Team extends AbstractEntity {

    @ManyToMany
    @JoinTable(joinColumns=@JoinColumn(name = "account_id", foreignKey = @ForeignKey(name="FK_account_id")),
            inverseJoinColumns=@JoinColumn(name = "team_id", nullable = false, foreignKey = @ForeignKey(name="FK_team_id")))
    private Set<Account> accounts = new HashSet<>();


    @ManyToMany
    @JoinTable(joinColumns=@JoinColumn(name = "project_id",  foreignKey = @ForeignKey(name="FK_project_id")),
            inverseJoinColumns=@JoinColumn(name = "team_id", nullable = false, foreignKey = @ForeignKey(name="FK_team_id")))
    private Set<Project> projects = new HashSet<>();

    @Column(nullable = false)
    private String name;

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
}