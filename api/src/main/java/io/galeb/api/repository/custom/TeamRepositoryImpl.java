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

package io.galeb.api.repository.custom;

import io.galeb.api.services.StatusService;
import io.galeb.core.entity.Account;
import io.galeb.core.entity.RoleGroup;
import io.galeb.core.entity.Team;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "SpringJavaAutowiredMembersInspection"})
public class TeamRepositoryImpl extends AbstractRepositoryImplementation<Team> implements TeamRepositoryCustom, WithRoles {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private StatusService statusService;

    @PostConstruct
    private void init() {
        setSimpleJpaRepository(Team.class, em);
        setStatusService(statusService);
    }

    @Override
    public Set<String> roles(Object principal, Object criteria) {
        Account account = (Account) principal;
        List<RoleGroup> roleGroups;
        if (criteria instanceof Team) {
            roleGroups = em.createNamedQuery("roleGroupsTeam", RoleGroup.class)
                    .setParameter("team_id", ((Team)criteria).getId())
                    .setParameter("account_id", account.getId())
                    .getResultList();
            return roleGroups.stream().flatMap(rg -> rg.getRoles().stream()).map(Enum::toString).collect(Collectors.toSet());
        }
        if (criteria instanceof Account) {
            roleGroups = em.createNamedQuery("roleGroupsFromTeams", RoleGroup.class)
                    .setParameter("account_id", account.getId())
                    .getResultList();
            return roleGroups.stream().flatMap(rg -> rg.getRoles().stream()).map(Enum::toString).collect(Collectors.toSet());
        }
        if (criteria instanceof Long) {
            Team team = em.find(Team.class, criteria);
            return roles(principal, team);
        }
        if (criteria instanceof String) {
            String query = "SELECT t FROM Team t WHERE t.name = :name";
            Team team = em.createQuery(query, Team.class).setParameter("name", criteria).getSingleResult();
            return roles(principal, team);
        }
        return Collections.emptySet();
    }

}
