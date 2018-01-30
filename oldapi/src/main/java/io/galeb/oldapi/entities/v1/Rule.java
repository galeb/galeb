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

package io.galeb.oldapi.entities.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public class Rule extends AbstractEntity<Rule> implements WithFarmID<Rule>, WithParents<VirtualHost> {

    private static final long serialVersionUID = 5596582746795373020L;

    @JsonProperty(required = true)
    private RuleType ruleType;

    private final Set<VirtualHost> parents = new HashSet<>();

    @JsonProperty(required = true)
    private Pool pool;

    @JsonIgnore
    private int ruleOrder = Integer.MAX_VALUE;

    @JsonIgnore
    private boolean ruleDefault = false;

    private final Set<VirtualHost> defaultIn = new HashSet<>();

    private Boolean global = false;

    @JsonIgnore
    private long farmId;

    public Rule(String name, RuleType ruleType, Pool pool) {
        Assert.notNull(ruleType, "[Assertion failed] - this argument is required; it must not be null");
        Assert.notNull(pool, "[Assertion failed] - this argument is required; it must not be null");
        setName(name);
        this.ruleType = ruleType;
        this.pool = pool;
    }

    public Rule() {
        //
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public Rule setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
        return this;
    }

    @Override
    public Set<VirtualHost> getParents() {
        return parents;
    }

    public Rule setParents(Set<VirtualHost> parents) {
        if (parents != null) {
            this.parents.clear();
            this.parents.addAll(parents);
        }
        return this;
    }

    public Pool getPool() {
        return pool;
    }

    public Rule setPool(Pool pool) {
        Assert.notNull(pool, "[Assertion failed] - this argument is required; it must not be null");
        this.pool = pool;
        return this;
    }

    @Override
    public long getFarmId() {
        return farmId;
    }

    @Override
    public Rule setFarmId(long farmId) {
        this.farmId = farmId;
        return this;
    }

    public int getRuleOrder() {
        return ruleOrder;
    }

    public Rule setRuleOrder(Integer ruleOrder) {
        if (ruleOrder != null) {
            this.ruleOrder = ruleOrder;
        }
        return this;
    }

    public boolean isRuleDefault() {
        return ruleDefault;
    }

    public Rule setRuleDefault(boolean ruleDefault) {
        this.ruleDefault = ruleDefault;
        return this;
    }

    public Set<VirtualHost> getDefaultIn() {
        return defaultIn;
    }

    public Rule setDefaultIn(Set<VirtualHost> defaultIn) {
        if (defaultIn != null) {
            this.defaultIn.clear();
            this.defaultIn.addAll(defaultIn);
        }
        return this;
    }

    public boolean isGlobal() {
        return global;
    }

    public Rule setGlobal(Boolean global) {
        if (global != null) {
            this.global = global;
        }
        return this;
    }

    @Override
    public EntityStatus getStatus() {
        return super.getDynamicStatus();
    }

    @Override
    @JsonIgnore
    public String getEnvName() {
        return getPool().getEnvName();
    }

    @Override
    @JsonIgnore
    public Farm getFarm() {
        return getFakeFarm();
    }

}
