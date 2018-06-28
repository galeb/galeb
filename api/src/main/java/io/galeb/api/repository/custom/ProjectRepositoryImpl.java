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
import io.galeb.core.entity.Project;
import io.galeb.core.entity.RoleGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "SpringJavaAutowiredMembersInspection"})
public class ProjectRepositoryImpl extends AbstractRepositoryImplementation<Project> implements ProjectRepositoryCustom, WithRoles {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private StatusService statusService;

    @PostConstruct
    private void init() {
        setSimpleJpaRepository(Project.class, em);
        setStatusService(statusService);
    }

    @Override
    protected long getProjectId(Object criteria) {
        Account account = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (criteria instanceof Project) {
            return ((Project) criteria).getId();
        }
        if (criteria instanceof Long) {
            Project project = em.find(Project.class, criteria);
            return project.getId();
        }
        if (criteria instanceof String) {
            String query = "SELECT t FROM Project t WHERE t.name = :name";
            Project project = em.createQuery(query, Project.class).setParameter("name", criteria).getSingleResult();
            return project.getId();
        }
        return -1;

    }

    @Override
    protected String querySuffix(String username) {
        return "INNER JOIN entity.teams t INNER JOIN t.accounts a WHERE a.username = '" + username + "'";
    }
}
