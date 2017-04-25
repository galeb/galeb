/*
 *   Galeb - Load Balance as a Service Plataform
 *
 *   Copyright (C) 2014-2015 Globo.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.galeb.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.Assert;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@MappedSuperclass
@JsonInclude(NON_NULL)
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_name_rule", columnNames = { "name" }) })
public class Rule extends AbstractEntity<Rule> implements WithFarmID<Rule>, WithParents<VirtualHost> {

    private static final long serialVersionUID = 5596582746795373020L;

    @ManyToOne
    @JoinColumn(name = "ruletype_id", nullable = false, foreignKey = @ForeignKey(name="FK_rule_ruletype"))
    @JsonProperty(required = true)
    private RuleType ruleType;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(joinColumns=@JoinColumn(name = "rule_id", foreignKey = @ForeignKey(name="FK_rule_parent")),
    inverseJoinColumns=@JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name="FK_parent_rule")))
    private final Set<VirtualHost> parents = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "pool_id", nullable = false, foreignKey = @ForeignKey(name="FK_rule_pool"))
    @JsonProperty(required = true)
    private Pool pool;

    @JsonIgnore
    @Transient
    private int ruleOrder = Integer.MAX_VALUE;

    @JsonIgnore
    @Transient
    private boolean ruleDefault = false;

    @OneToMany(mappedBy = "ruleDefault")
    private final Set<VirtualHost> defaultIn = new HashSet<>();

    @Column
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

    protected Rule() {
        //
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public Rule setRuleType(RuleType ruleType) {
        updateHash();
        this.ruleType = ruleType;
        return this;
    }

    @Override
    public Set<VirtualHost> getParents() {
        return parents;
    }

    public Rule setParents(Set<VirtualHost> parents) {
        if (parents != null) {
            updateHash();
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
        updateHash();
        this.pool = pool;
        return this;
    }

    @Override
    public long getFarmId() {
        return farmId;
    }

    @Override
    public Rule setFarmId(long farmId) {
        updateHash();
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
            updateHash();
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
            updateHash();
            this.global = global;
        }
        return this;
    }

}
