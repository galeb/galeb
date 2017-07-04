/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.Assert;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_name_account", columnNames = { "name" }) })
public class Account extends AbstractEntity<Account> {

    private static final long serialVersionUID = -2745836665462717899L;

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    public enum Role {
        ROLE_USER,
        ROLE_ADMIN
    }

    @ManyToMany
    @JoinTable(joinColumns = @JoinColumn(name = "account_id",
                                         foreignKey = @ForeignKey(name = "FK_account_teams_account_id")),
               inverseJoinColumns = @JoinColumn(name = "team_id",
                                       foreignKey = @ForeignKey(name = "FK_account_teams_team_id")))
    private final Set<Team> teams = new HashSet<>();

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @JsonProperty(required = true)
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @JoinColumn(foreignKey=@ForeignKey(name="FK_account_roles"))
    private final Set<Role> roles = new HashSet<>();

    public String getEmail() {
        return email;
    }

    public Account setEmail(String email) {
        Assert.hasText(email, "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");
        updateHash();
        this.email = email;
        return this;
    }

    public Set<Team> getTeams() {
        return teams;
    }

    public Account setTeams(Set<Team> teams) {
        if (teams != null) {
            updateHash();
            this.teams.clear();
            this.teams.addAll(teams);
        }
        return this;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public Account setRoles(Set<Role> roles) {
        if (roles != null) {
            updateHash();
            this.roles.clear();
            this.roles.addAll(roles);
        }
        return this;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonProperty(value = "password", required = true)
    public Account setPassword(String password) {
        Assert.hasText(password, "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");
        updateHash();
        this.password = ENCODER.encode(password);
        return this;
    }
}
