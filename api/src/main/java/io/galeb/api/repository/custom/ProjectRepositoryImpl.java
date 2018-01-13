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
import io.galeb.core.entity.Project;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
        Project project = null;
        if (criteria instanceof Project) {
            project = em.find(Project.class, ((Project) criteria).getId());
        }
        if (criteria instanceof Long) {
            project = em.find(Project.class, criteria);
        }
        if (criteria instanceof String) {
            String query = "SELECT p FROM Project p WHERE p.name = :name";
            project = em.createQuery(query, Project.class).setParameter("name", criteria).getSingleResult();
        }
        return project != null ? project.getId() : -1L;
    }

    @Override
    protected String querySuffix(String username) {
        return "INNER JOIN entity.teams t INNER JOIN t.accounts a WHERE a.username = '" + username + "'";
    }
}
