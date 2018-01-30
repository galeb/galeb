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
public class Pool extends AbstractEntity<Pool> implements WithFarmID<Pool> {

    private static final long serialVersionUID = 5596582746795373021L;

    private Environment environment;

    @JsonIgnore
    private long farmId;

    private final Set<Target> targets = new HashSet<>();

    @JsonIgnore
    private final Set<Rule> rules = new HashSet<>();

    private Project project;

    private BalancePolicy balancePolicy;

    private Boolean global = false;

    public Pool(String name) {
        setName(name);
    }

    public Pool() {
        //
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Pool setEnvironment(Environment environment) {
        Assert.notNull(environment, "[Assertion failed] - this argument is required; it must not be null");
        this.environment = environment;
        return this;
    }

    @Override
    public long getFarmId() {
        return farmId;
    }

    @Override
    public Pool setFarmId(long farmId) {
        this.farmId = farmId;
        return this;
    }

    @Override
    @JsonIgnore
    public Farm getFarm() {
        return null;
    }

    public Set<Target> getTargets() {
        return targets;
    }

    public Pool setTargets(Set<Target> targets) {
        if (targets != null) {
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
        this.project = project;
        return this;
    }

    public BalancePolicy getBalancePolicy() {
        return balancePolicy;
    }

    public Pool setBalancePolicy(BalancePolicy balancePolicy) {
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
            this.global = global;
        }
        return this;
    }

    @Override
    public EntityStatus getStatus() {
        return super.getDynamicStatus();
    }

}
