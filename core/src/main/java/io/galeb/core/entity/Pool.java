/*
 * Galeb - Load Balance as a Service Plataform
 *
 * Copyright (C) 2014-2015 Globo.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
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
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@MappedSuperclass
@JsonInclude(NON_NULL)
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_name_pool", columnNames = { "name" }) })
public class Pool extends AbstractEntity<Pool> implements WithFarmID<Pool> {

    private static final long serialVersionUID = 5596582746795373021L;

    @ManyToOne
    @JoinColumn(name = "environment_id", nullable = false, foreignKey = @ForeignKey(name="FK_pool_environment"))
    private Environment environment;

    @JsonIgnore
    private long farmId;

    @OneToMany(mappedBy = "parent")
    private final Set<Target> targets = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "pool", fetch = FetchType.EAGER)
    private final Set<Rule> rules = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name="FK_pool_project"))
    private Project project;

    @ManyToOne
    @JoinColumn(name = "balancepolicy_id", foreignKey = @ForeignKey(name="FK_pool_balancepolicy"))
    private BalancePolicy balancePolicy;

    @Column(insertable = false, updatable = false, nullable = false)
    private Boolean global = false;

    public Pool(String name) {
        setName(name);
    }

    protected Pool() {
        //
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Pool setEnvironment(Environment environment) {
        Assert.notNull(environment, "[Assertion failed] - this argument is required; it must not be null");
        updateHash();
        this.environment = environment;
        return this;
    }

    @Override
    public long getFarmId() {
        return farmId;
    }

    @Override
    public Pool setFarmId(long farmId) {
        updateHash();
        this.farmId = farmId;
        return this;
    }

    public Set<Target> getTargets() {
        return targets;
    }

    public Pool setTargets(Set<Target> targets) {
        if (targets != null) {
            updateHash();
            this.targets.clear();
            this.targets.addAll(targets);
        }
        return this;
    }

    public Set<Rule> getRules() {
        return rules;
    }

    public Pool setRules(Set<Rule> rules) {
        if (rules != null) {
            updateHash();
            this.rules.clear();
            this.rules.addAll(rules);
        }
        return this;
    }

    public Project getProject() {
        return project;
    }

    public Pool setProject(Project project) {
        Assert.notNull(project, "[Assertion failed] - this argument is required; it must not be null");
        updateHash();
        this.project = project;
        return this;
    }

    public BalancePolicy getBalancePolicy() {
        return balancePolicy;
    }

    public Pool setBalancePolicy(BalancePolicy balancePolicy) {
        updateHash();
        this.balancePolicy = balancePolicy;
        return this;
    }

    @JsonProperty("_global")
    public boolean isGlobal() {
        return global;
    }

    @JsonIgnore
    public Pool setGlobal(Boolean global) {
        if (global != null) {
            updateHash();
            this.global = global;
        }
        return this;
    }

}
