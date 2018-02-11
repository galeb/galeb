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
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

public class VirtualHost extends AbstractEntity<VirtualHost> implements WithFarmID<VirtualHost>, WithAliases<VirtualHost> {

    private static final long serialVersionUID = 5596582746795373014L;

    @JsonProperty(required = true)
    private Environment environment;

    @JsonProperty(required = true)
    private Project project;

    private final Set<String> aliases = new HashSet<>();

    @JsonIgnore
    private long farmId;

    @JsonProperty("rulesOrdered")
    private final Set<RuleOrder> rulesOrdered = new HashSet<>();

    private Rule ruleDefault;

    private final Set<Rule> rules = new HashSet<>();

    public VirtualHost(String name, Environment environment, Project project) {
        Assert.notNull(environment, "[Assertion failed] - this argument is required; it must not be null");
        Assert.notNull(project, "[Assertion failed] - this argument is required; it must not be null");
        setName(name);
        this.environment = environment;
        this.project = project;
    }

    public VirtualHost() {
        //
    }

    public Environment getEnvironment() {
        return environment;
    }

    public VirtualHost setEnvironment(Environment environment) {
        this.environment = environment;
        return this;
    }

    public Project getProject() {
        return project;
    }

    public VirtualHost setProject(Project project) {
        this.project = project;
        return this;
    }

    @Override
    public long getFarmId() {
        return farmId;
    }

    @Override
    public VirtualHost setFarmId(long farmId) {
        this.farmId = farmId;
        return this;
    }

    @Override
    public Set<String> getAliases() {
        return aliases;
    }

    @Override
    public VirtualHost setAliases(Set<String> aliases) {
        if (aliases != null) {
            this.aliases.clear();
            this.aliases.addAll(aliases);
        }
        return this;
    }

    public Set<RuleOrder> getRulesOrdered() {
        return rulesOrdered;
    }

    public VirtualHost setRulesOrdered(Set<RuleOrder> rulesOrdered) {
        if (rulesOrdered != null) {
            this.rulesOrdered.clear();
            this.rulesOrdered.addAll(rulesOrdered);
        }
        return this;
    }

    public Rule getRuleDefault() {
        return ruleDefault;
    }

    public VirtualHost setRuleDefault(Rule ruleDefault) {
        this.ruleDefault = ruleDefault;
        return this;
    }

    public Set<Rule> getRules() {
        return rules;
    }

    public VirtualHost setRules(Set<Rule> rules) {
        if (rules != null) {
            this.rules.clear();
            this.rules.addAll(rules);
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
        return getEnvironment().getName();
    }

    @Override
    @JsonIgnore
    public Farm getFarm() {
        return getFakeFarm();
    }

}
