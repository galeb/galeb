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

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "ruleordered", uniqueConstraints = { @UniqueConstraint(name = "UK_order__rule_id__virtualhostgroup_id__environment_id", columnNames = { "rule_order", "virtualhostgroup_ruleordered_id", "rule_ruleordered_id", "environment_id" }) })
public class RuleOrdered extends AbstractEntity implements WithStatus, Comparable<RuleOrdered> {

    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JsonProperty("virtualhostgroup")
    @JoinColumn(name = "virtualhostgroup_ruleordered_id", nullable = false, foreignKey = @ForeignKey(name="FK_virtualhostgroup_ruleordered"))
    private VirtualhostGroup virtualhostgroup;

    @JsonProperty("order")
    @Column(name = "rule_order", nullable = false)
    private Integer order = Integer.MAX_VALUE;

    @ManyToOne
    @JoinColumn(name = "rule_ruleordered_id", nullable = false, foreignKey = @ForeignKey(name="FK_rule_ruleordered"))
    private Rule rule;

    @ManyToOne(optional = false)
    @JoinColumn(name = "environment_id", nullable = false, foreignKey = @ForeignKey(name="FK_ruleordered_environment"))
    private Environment environment;

    @Transient
    private Map<Long, Status> status = new HashMap<>();

    public VirtualhostGroup getVirtualhostgroup() {
        return virtualhostgroup;
    }

    public void setVirtualhostgroup(VirtualhostGroup virtualhostgroup) {
        this.virtualhostgroup = virtualhostgroup;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
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
        RuleOrdered that = (RuleOrdered) o;
        return Objects.equals(rule, that.rule);
    }

    @Override
    public int hashCode() {

        return Objects.hash(rule);
    }

    @Override
    public int compareTo(RuleOrdered o) {
        return getRule().equals(o.getRule()) ? 0 : getOrder().compareTo(o.getOrder());
    }
}
