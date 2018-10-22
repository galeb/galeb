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

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@NamedQueries({
        @NamedQuery(
                name = "VirtualhostGroupDefault",
                query = "SELECT DISTINCT entity From VirtualhostGroup entity INNER JOIN entity.virtualhosts v INNER JOIN v.project.teams t INNER JOIN t.accounts a " +
                "WHERE a.username = :username")
})

@Entity
@Table(name = "virtualhostgroup")
public class VirtualhostGroup extends AbstractEntity implements WithStatus {

    @JsonManagedReference
    @OneToMany(mappedBy = "virtualhostgroup", cascade = CascadeType.REMOVE)
    public Set<VirtualHost> virtualhosts = new HashSet<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "virtualhostgroup", cascade = CascadeType.REMOVE)
    public Set<RuleOrdered> rulesordered = new HashSet<>();

    @Transient
    private Map<Long, Status> status = new HashMap<>();

    public Set<VirtualHost> getVirtualhosts() {
        return virtualhosts;
    }

    public void setVirtualhosts(Set<VirtualHost> virtualhosts) {
        if (virtualhosts != null) {
            this.virtualhosts.clear();
            this.virtualhosts.addAll(virtualhosts);
        }
    }

    public Set<RuleOrdered> getRulesordered() {
        return rulesordered;
    }

    public void setRulesordered(Set<RuleOrdered> rulesordered) {
        if (rulesordered != null) {
            this.rulesordered.clear();
            this.rulesordered.addAll(rulesordered);
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
}
