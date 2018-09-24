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
import io.galeb.core.entity.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
    public Set<String> roles(Object criteria) {
        Account account = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (criteria instanceof Team) {
            if (isAccountLinkedWithTeam(account.getId(), ((Team)criteria).getId())) {
                return mergeAllRolesOf(account);
            }
        }
        if (criteria instanceof Long) {
            Team team = em.find(Team.class, criteria);
            return roles(team);
        }
        if (criteria instanceof String) {
            String query = "SELECT t FROM Team t WHERE t.name = :name";
            Team team = em.createQuery(query, Team.class).setParameter("name", criteria).getSingleResult();
            return roles(team);
        }
        return Collections.emptySet();
    }

    @Override
    protected String querySuffix(String username) {
        return "INNER JOIN entity.accounts a WHERE a.username = '" + username + "'";
    }

    private boolean isAccountLinkedWithTeam(long accountId, long teamId) {
        List<Team> teams = em.createNamedQuery("teamLinkedToAccount", Team.class)
                .setParameter("account_id", accountId)
                .setParameter("team_id", teamId).getResultList();
        return teams != null && !teams.isEmpty();
    }


}
