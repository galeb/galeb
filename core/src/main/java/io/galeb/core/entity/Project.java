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

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_name_project", columnNames = { "name" }) })
public class Project extends AbstractEntity<Project> {

    private static final long serialVersionUID = 5596582746795373018L;

    @JsonIgnore
    @OneToMany(mappedBy = "project")
    private final Set<VirtualHost> virtualhosts = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "project")
    private final Set<Target> targets = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "project")
    private final Set<Pool> pools = new HashSet<>();

    @ManyToMany
    @JoinTable(joinColumns=@JoinColumn(name="project_id"),
               inverseJoinColumns=@JoinColumn(name="team_id"))
    private final Set<Team> teams = new HashSet<>();

    public Project(String name) {
        setName(name);
    }

    protected Project() {
        //
    }

    public Set<Team> getTeams() {
        return teams;
    }

    public Project setTeams(Set<Team> teams) {
        if (teams != null) {
            updateHash();
            this.teams.clear();
            this.teams.addAll(teams);
        }
        return this;
    }
}
