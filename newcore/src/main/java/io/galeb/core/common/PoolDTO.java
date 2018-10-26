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

package io.galeb.core.common;


import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.BalancePolicy;
import io.galeb.core.entity.Environment;
import io.galeb.core.entity.HealthCheck;
import io.galeb.core.entity.Project;
import io.galeb.core.entity.Rule;
import io.galeb.core.entity.Target;
import io.galeb.core.entity.WithGlobal;
import io.galeb.core.entity.WithStatus;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import org.springframework.util.Assert;


public class PoolDTO extends AbstractEntity implements WithStatus, WithGlobal {


    private Set<Rule> rules= new HashSet<>();


    private Environment environment;


    private Set<Target> targets = new HashSet<>();


    private Project project;


    private BalancePolicy balancepolicy;

    private String name;

    private Boolean global = false;

    private Long poolSize = -1L;

    private Map<Long, Status> status = new HashMap<>();

    private String allow = "/";

    // Healthcheck Attributes

    private String hcPath = "/";

    private String hcHttpStatusCode;

    private String hcHost;

    private Boolean hcTcpOnly = true;

    private HealthCheck.HttpMethod hcHttpMethod = HealthCheck.HttpMethod.GET;

    private String hcBody;

    private Map<String, String> hcHeaders = new HashMap<>();

    public Set<Rule> getRules() {
        return rules;
    }

    public void setRules(Set<Rule> rules) {
        if (rules != null) {
            this.rules.clear();
            this.rules.addAll(rules);
        }
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Set<Target> getTargets() {
        return targets;
    }

    public void setTargets(Set<Target> targets) {
        if (targets != null) {
            this.targets.clear();
            this.targets.addAll(targets);
        }
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        Assert.notNull(project, "Project is NULL");
        this.project = project;
    }

    public BalancePolicy getBalancepolicy() {
        return balancepolicy;
    }

    public void setBalancepolicy(BalancePolicy balancepolicy) {
        Assert.notNull(balancepolicy, "BalancePolicy is NULL");
        this.balancepolicy = balancepolicy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Assert.hasText(name, "name is not valid");
        this.name = name;
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

    public String getAllow() { return allow; }

    public void setAllow(String allow) { this.allow = allow; }

    public String getHcPath() {
        return hcPath;
    }

    public void setHcPath(String hcPath) {
        this.hcPath = hcPath;
    }

    public String getHcHttpStatusCode() {
        return hcHttpStatusCode;
    }

    public void setHcHttpStatusCode(String hcHttpStatusCode) {
        this.hcHttpStatusCode = hcHttpStatusCode;
    }

    public String getHcHost() {
        return hcHost;
    }

    public void setHcHost(String hcHost) {
        this.hcHost = hcHost;
    }

    public Boolean getHcTcpOnly() {
        return hcTcpOnly;
    }

    public void setHcTcpOnly(Boolean hcTcpOnly) {
        if (hcTcpOnly != null) {
            this.hcTcpOnly = hcTcpOnly;
        }
    }

    public HealthCheck.HttpMethod getHcHttpMethod() {
        return hcHttpMethod;
    }

    public void setHcHttpMethod(HealthCheck.HttpMethod hcHttpMethod) {
        if (hcHttpMethod != null) {
            this.hcHttpMethod = hcHttpMethod;
        }
    }

    public String getHcBody() {
        return hcBody;
    }

    public void setHcBody(String hcBody) {
        this.hcBody = hcBody;
    }

    public Map<String, String> getHcHeaders() {
        return hcHeaders;
    }

    public void setHcHeaders(Map<String, String> hcHeaders) {
        if (hcHeaders != null) {
            this.hcHeaders.clear();
            this.hcHeaders.putAll(hcHeaders);
        }
    }

    public Long getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(Long poolSize) {
        this.poolSize = poolSize;
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
        PoolDTO pool = (PoolDTO) o;
        return Objects.equals(getProject(), pool.getProject()) &&
                Objects.equals(getName(), pool.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProject(), getName());
    }
}
