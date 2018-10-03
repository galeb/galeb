/*
 * Copyright (c) 2014-2018 Globo.com - ATeam
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

import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@NamedQueries({
        @NamedQuery(
                name = "projectLinkedToAccount",
                query = "SELECT p FROM Project p INNER JOIN p.teams t INNER JOIN t.accounts a WHERE a.id = :account_id AND p.id = :project_id"),
        @NamedQuery(
                name = "projectHealthStatus",
                query = "SELECT p FROM Project p INNER JOIN p.pools pools INNER JOIN pools.targets t INNER JOIN t.healthStatus h WHERE h.id = :id"),
        @NamedQuery(
                name = "projectsFromRuleOrdered",
                query = "SELECT p FROM Project p INNER JOIN p.rules r INNER JOIN r.rulesOrdered ro WHERE ro.id = :id"),
        @NamedQuery(
                name = "projectFromVirtualhostGroup",
                query = "SELECT p FROM Project p INNER JOIN p.virtualhosts v WHERE v.virtualhostgroup.id = :id"),
        @NamedQuery(
                name = "projectFromTarget",
                query = "SELECT p FROM Project p INNER JOIN p.pools pools INNER JOIN pools.targets t WHERE t.id = :id"),
        @NamedQuery(
                name = "ProjectDefault",
                query = "SELECT DISTINCT entity From Project entity INNER JOIN entity.teams t INNER JOIN t.accounts a WHERE a.username = :username")
})

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_project_name", columnNames = { "name" }) })
public class Project extends AbstractEntity {

    @OneToMany(mappedBy = "project")
    private Set<Rule> rules = new HashSet<>();

    @OneToMany(mappedBy = "project")
    private Set<Pool> pools = new HashSet<>();

    @ManyToMany
    @JoinTable(joinColumns=@JoinColumn(name = "project_id",  foreignKey = @ForeignKey(name="FK_team_project_id")),
            inverseJoinColumns=@JoinColumn(name = "team_id", nullable = false, foreignKey = @ForeignKey(name="FK_project_team_id")))
    private Set<Team> teams = new HashSet<>();

    @OneToMany(mappedBy = "project")
    private Set<VirtualHost> virtualhosts = new HashSet<>();

    @ManyToMany(mappedBy = "projects", fetch = FetchType.EAGER)
    private Set<RoleGroup> rolegroups = new HashSet<>();

    @Column(nullable = false)
    private String name;

    public Set<Rule> getRules() {
        return rules;
    }

    public void setRules(Set<Rule> rules) {
        if (rules != null) {
            this.rules.clear();
            this.rules.addAll(rules);
        }
    }

    public Set<Pool> getPools() {
        return pools;
    }

    public void setPools(Set<Pool> pools) {
        if (pools != null) {
            this.pools.clear();
            this.pools.addAll(pools);
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

    public Set<VirtualHost> getVirtualhosts() {
        return virtualhosts;
    }

    public void setVirtualhosts(Set<VirtualHost> virtualhosts) {
        if (virtualhosts != null) {
            this.virtualhosts.clear();
            this.virtualhosts.addAll(virtualhosts);
        }
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Assert.hasText(name, "name is not valid");
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(getName(), project.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
