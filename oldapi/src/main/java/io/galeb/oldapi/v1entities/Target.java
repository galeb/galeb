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

package io.galeb.oldapi.v1entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public class Target extends AbstractEntity<Target> implements WithFarmID<Target>, WithParent<Pool> {

    private static final long serialVersionUID = 5596582746795373012L;

    private Environment environment;

    @JsonIgnore
    private long farmId;

    private Pool parent;

    private Project project;

    private Boolean global = false;

    public Target(String name) {
        setName(name);
    }

    protected Target() {
        //
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Target setEnvironment(Environment environment) {
        this.environment = environment;
        return this;
    }

    @Override
    public long getFarmId() {
        return farmId;
    }

    @Override
    public Target setFarmId(long farmId) {
        this.farmId = farmId;
        return this;
    }

    @Override
    public Pool getParent() {
        return parent;
    }

    public Target setParent(Pool parent) {
        if (parent != null) {
            this.parent = parent;
        }
        return this;
    }

    public Project getProject() {
        return project;
    }

    public Target setProject(Project project) {
        this.project = project;
        return this;
    }

    @JsonProperty("_global")
    public boolean isGlobal() {
        return global;
    }

    @JsonIgnore
    public Target setGlobal(Boolean global) {
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
        return getEnvironment().getName();
    }

    @Override
    @JsonIgnore
    public Farm getFarm() {
        return getFakeFarm();
    }
}
