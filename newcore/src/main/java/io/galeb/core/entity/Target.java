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
                name = "TargetDefault",
                query = "SELECT DISTINCT entity From Target entity WHERE entity.id IN " +
                            "(SELECT entity.id FROM Target entity INNER JOIN entity.pool pool " +
                            "INNER JOIN pool.project p INNER JOIN p.teams t INNER JOIN t.accounts a " +
                            "WHERE a.username = :username AND pool.global = false) " +
                        "OR entity.id IN " +
                            "(SELECT entity.id FROM Target entity INNER JOIN entity.pool pool WHERE pool.global = true) " +
                        "OR entity.id IN " +
                            "(SELECT entity.id FROM Target entity INNER JOIN entity.pool pool INNER JOIN pool.rules r WHERE r.global = true)")
})
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_target_name_pool_id", columnNames = { "name", "pool_id" }) })
public class Target extends AbstractEntity implements WithStatus {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id", nullable = false, foreignKey = @ForeignKey(name="FK_target_pool"))
    private Pool pool;


    @OneToMany(fetch = FetchType.EAGER, mappedBy = "target", cascade = CascadeType.REMOVE)
    private Set<HealthStatus> healthStatus = new HashSet<>();

    @Column(nullable = false)
    private String name;

    @Transient
    private Map<Long, Status> status = new HashMap<>();

    public Pool getPool() {
        return pool;
    }

    public void setPool(Pool pool) {
        this.pool = pool;
    }

    public Set<HealthStatus> getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(Set<HealthStatus> healthStatus) {
        if (healthStatus != null) {
            this.healthStatus.clear();
            this.healthStatus.addAll(healthStatus);
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
        Target target = (Target) o;
        return Objects.equals(getName(), target.getName()) || Objects.equals(getId(), target.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
