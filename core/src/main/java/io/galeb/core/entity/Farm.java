/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Collections;
import java.util.List;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_name_farm", columnNames = { "name" }) })
public class Farm extends AbstractEntity<Farm> {

    private static final long serialVersionUID = 5596582746795373017L;

    @Column(nullable = false)
    @JsonProperty(required = true)
    private String domain;

    @Column(nullable = false)
    @JsonProperty(required = true)
    private String api;

    @ManyToOne
    @JoinColumn(name = "environment_id", nullable = false, foreignKey = @ForeignKey(name="FK_farm_environment"))
    @JsonProperty(required = true)
    private Environment environment;

    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false, foreignKey = @ForeignKey(name="FK_farm_provider"))
    @JsonProperty(required = true)
    private Provider provider;

    @Column
    private boolean autoReload = true;

    public Farm(String name,String domain, String api, Environment environment, Provider provider) {
        Assert.hasText(domain, "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");
        Assert.hasText(api, "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");
        Assert.notNull(environment, "[Assertion failed] - this argument is required; it must not be null");
        Assert.notNull(provider, "[Assertion failed] - this argument is required; it must not be null");
        setName(name);
        this.domain = domain;
        this.api = api;
        this.environment = environment;
        this.provider = provider;
    }

    protected Farm() {
        //
    }

    public String getDomain() {
        return domain;
    }

    public Farm setDomain(String domain) {
        Assert.hasText(domain, "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");
        updateHash();
        this.domain = domain;
        return this;
    }

    public String getApi() {
        return api;
    }

    public Farm setApi(String api) {
        Assert.hasText(api, "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");
        updateHash();
        this.api = api;
        return this;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Farm setEnvironment(Environment environment) {
        Assert.notNull(environment, "[Assertion failed] - this argument is required; it must not be null");
        updateHash();
        this.environment = environment;
        return this;
    }

    public Provider getProvider() {
        return provider;
    }

    public Farm setProvider(Provider provider) {
        Assert.notNull(provider, "[Assertion failed] - this argument is required; it must not be null");
        updateHash();
        this.provider = provider;
        return this;
    }

    public boolean isAutoReload() {
        return autoReload;
    }

    public Farm setAutoReload(boolean autoReload) {
        updateHash();
        this.autoReload = autoReload;
        return this;
    }

    @Override
    public EntityStatus getStatus() {
        return status;
    }

    public List<LockStatus> getLockStatus() {
        return Collections.emptyList();
    }
}
