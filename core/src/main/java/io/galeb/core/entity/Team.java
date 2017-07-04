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

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_name_team", columnNames = { "name" }) })
public class Team extends AbstractEntity<Team> {

    private static final long serialVersionUID = -4278444359290384175L;

    @ManyToMany(mappedBy = "teams")
    private final Set<Account> accounts = new HashSet<>();

    @ManyToMany(mappedBy = "teams")
    private final Set<Project> projects = new HashSet<>();

    public Set<Account> getAccounts() {
        return accounts;
    }

    public Team setAccounts(Set<Account> accounts) {
        if (accounts != null) {
            updateHash();
            this.accounts.clear();
            this.accounts.addAll(accounts);
        }
        return this;
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public Team setProjects(Set<Project> projects) {
        if (projects != null) {
            updateHash();
            this.projects.clear();
            this.projects.addAll(projects);
        }
        return this;
    }

}
