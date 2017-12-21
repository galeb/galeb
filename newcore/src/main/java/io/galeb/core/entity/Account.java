package io.galeb.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.google.common.hash.Hashing.sha256;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_account_name", columnNames = { "name" }) })
public class Account extends AbstractEntity  {

    @JsonProperty(required = true)
    @Column(nullable = false)
    private String email;

    @JsonProperty(required = true)
    @Column(nullable = false)
    private String name;

    private String apitoken = sha256().hashBytes(UUID.randomUUID().toString().getBytes()).toString();

    @ManyToMany(mappedBy = "accounts")
    private Set<Team> teams = new HashSet<>();

    public Set<Team> getTeams() {
        return teams;
    }

    public void setTeams(Set<Team> teams) {
        if (teams != null) {
            this.teams.clear();
            this.teams.addAll(teams);
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        Assert.hasText(email, "email is not valid");
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Assert.hasText(name, "name is not valid");
        this.name = name;
    }

    @JsonIgnore
    public String getApitoken() {
        return apitoken;
    }

    public void setApitoken(String seed) {
        this.apitoken = sha256().hashBytes((seed + UUID.randomUUID().toString()).getBytes()).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(getName(), account.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
