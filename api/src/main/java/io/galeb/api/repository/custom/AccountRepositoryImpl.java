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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "SpringJavaAutowiredMembersInspection"})
public class AccountRepositoryImpl extends AbstractRepositoryImplementation<Account> implements AccountRepositoryCustom, WithRoles {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private StatusService statusService;

    @PostConstruct
    private void init() {
        setSimpleJpaRepository(Account.class, em);
        setStatusService(statusService);
    }

    @Override
    public Set<String> roles(Object criteria) {
        Account account = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Set<String> roles = account.getRolegroups().stream().flatMap(rg -> rg.getRoles().stream())
                .map(Object::toString).distinct().collect(Collectors.toSet());
        List<RoleGroup> roleGroups = em.createNamedQuery("roleGroupsFromTeams", RoleGroup.class)
                .setParameter("id", account.getId())
                .getResultList();
        roles.addAll(roleGroups.stream().flatMap(rg -> rg.getRoles().stream())
                .map(Object::toString).distinct().collect(Collectors.toSet()));
        return roles;
    }

    @Override
    protected String querySuffix(String username) {
        return "WHERE entity.username = '" + username + "'";
    }

}
