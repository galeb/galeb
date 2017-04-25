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

import javax.persistence.Column;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@MappedSuperclass
@JsonInclude(NON_NULL)
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_name_parent_id_target", columnNames = { "name", "parent_id" }) })
public class Target extends AbstractEntity<Target> implements WithFarmID<Target>, WithParent<Pool> {

    private static final long serialVersionUID = 5596582746795373012L;

    @ManyToOne
    @JoinColumn(name = "environment_id", foreignKey = @ForeignKey(name="FK_target_environment"))
    private Environment environment;

    @JsonIgnore
    private long farmId;

    @ManyToOne
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name="FK_target_parent"))
    private Pool parent;

    @ManyToOne
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name="FK_target_project"))
    private Project project;

    @Column(insertable = false, updatable = false, nullable = false)
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
        updateHash();
        this.environment = environment;
        return this;
    }

    @Override
    public long getFarmId() {
        return farmId;
    }

    @Override
    public Target setFarmId(long farmId) {
        updateHash();
        this.farmId = farmId;
        return this;
    }

    public Pool getParent() {
        return parent;
    }

    public Target setParent(Pool parent) {
        if (parent != null) {
            updateHash();
            this.parent = parent;
        }
        return this;
    }

    public Project getProject() {
        return project;
    }

    public Target setProject(Project project) {
        updateHash();
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
            updateHash();
            this.global = global;
        }
        return this;
    }

}
