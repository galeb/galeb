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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_environment_name", columnNames = { "name" }) })
public class Environment extends AbstractEntity implements WithStatus {

    @ManyToMany(mappedBy = "environments")
    private Set<VirtualHost> virtualhosts = new HashSet<>();

    @OneToMany(mappedBy = "environment")
    private Set<Pool> pools = new HashSet<>();

    @OneToMany(mappedBy = "environment")
    private Set<RuleOrdered> rulesordered = new HashSet<>();

    @Column(nullable = false)
    private String name;

    @Transient
    private Map<Long, Status> status = new HashMap<>();

    public Set<VirtualHost> getVirtualhosts() {
        return virtualhosts;
    }

    public void setVirtualhosts(Set<VirtualHost> virtualHosts) {
        if (virtualHosts != null) {
            this.virtualhosts.clear();
            this.virtualhosts.addAll(virtualHosts);
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

    public Set<RuleOrdered> getRulesordered() {
        return rulesordered;
    }

    public void setRulesordered(Set<RuleOrdered> rulesordered) {
        this.rulesordered = rulesordered;
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
        Environment that = (Environment) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getName());
    }
}

