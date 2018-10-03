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
import java.util.*;

@NamedQueries({
        @NamedQuery(
                name = "RuleDefault",
                query = "SELECT DISTINCT entity From Rule entity WHERE entity.id IN " +
                    "(SELECT entity.id FROM Rule entity INNER JOIN entity.project.teams t INNER JOIN t.accounts a " +
                        "WHERE a.username = :username AND entity.global = false) " +
                "OR entity.id IN " +
                    "(SELECT entity.id From Rule entity WHERE entity.global = true)")
})
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_rule_name", columnNames = { "name" }) })
public class Rule extends AbstractEntity implements WithStatus, WithGlobal {

    @ManyToMany
    @JoinTable(joinColumns=@JoinColumn(name = "rule_id", foreignKey = @ForeignKey(name="FK_pool_rule_id")),
            inverseJoinColumns=@JoinColumn(name = "pool_id", foreignKey = @ForeignKey(name="FK_rule_pool_id")))
    private Set<Pool> pools = new HashSet<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name="FK_rule_project"))
    private Project project;

    @Column(nullable = false)
    private String matching;

    private Boolean global = false;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.REMOVE)
    private List<RuleOrdered> rulesOrdered = new ArrayList<>();

    @Transient
    private Map<Long, Status> status = new HashMap<>();

    public Set<Pool> getPools() {
        return pools;
    }

    public void setPools(Set<Pool> pools) {
        if (pools != null) {
            this.pools.clear();
            this.pools.addAll(pools);
        }
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        Assert.notNull(project, "Project is NULL");
        this.project = project;
    }

    public String getMatching() {
        return matching;
    }

    public void setMatching(String matching) {
        Assert.hasText(matching, "matching is not valid");
        this.matching = matching;
    }

    @Override
    public Boolean getGlobal() {
        return global;
    }

    public void setGlobal(Boolean global) {
        if (global != null) {
            this.global = global;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Assert.hasText(name, "name is not valid");
        this.name = name;
    }

    public List<RuleOrdered> getRulesOrdered() {
        return rulesOrdered;
    }

    public void setRulesOrdered(List<RuleOrdered> rulesOrdered) {
        if (rulesOrdered != null) {
            this.rulesOrdered.clear();
            this.rulesOrdered.addAll(rulesOrdered);
        }
    }

    @Override
    public Map<Long, Status> getStatus() {
        return status;
    }

    @Override
    public void setStatus(Map<Long, Status> status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rule rule = (Rule) o;
        return Objects.equals(name, rule.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
