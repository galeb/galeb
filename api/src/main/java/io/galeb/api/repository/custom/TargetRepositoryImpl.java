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

import io.galeb.api.repository.EnvironmentRepository;
import io.galeb.api.services.StatusService;
import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.Environment;
import io.galeb.core.entity.Project;
import io.galeb.core.entity.Target;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"unused", "SpringJavaAutowiredMembersInspection"})
public class TargetRepositoryImpl extends AbstractRepositoryImplementation<Target> implements TargetRepositoryCustom, WithRoles {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private StatusService statusService;

    @Autowired
    private EnvironmentRepository environmentRepository;

    @PostConstruct
    private void init() {
        setSimpleJpaRepository(Target.class, em);
        setStatusService(statusService);
    }

    @Override
    protected Set<Environment> getAllEnvironments(AbstractEntity entity) {
        return environmentRepository.findAllByTargetId(entity.getId());
    }

    @Override
    protected long getProjectId(Object criteria) {
        Target target = null;
        try {
            if (criteria instanceof Target) {
                target = em.find(Target.class, ((Target) criteria).getId());
            }
            if (criteria instanceof Long) {
                target = em.find(Target.class, criteria);
            }
            if (criteria instanceof String) {
                String query = "SELECT t FROM Target t WHERE t.name = :name";
                target = em.createQuery(query, Target.class).setParameter("name", criteria).getSingleResult();
            }
        } catch (Exception ignored) {}
        if (target == null) {
            return -1L;
        }
        List<Project> projects = em.createNamedQuery("projectFromTarget", Project.class)
                .setParameter("id", target.getId())
                .getResultList();
        if (projects == null || projects.isEmpty()) {
            return -1;
        }
        return projects.stream().map(AbstractEntity::getId).findAny().orElse(-1L);
    }

    @Override
    protected String querySuffix(String username) {
        return "INNER JOIN entity.pools p LEFT JOIN p.project.teams t INNER JOIN t.accounts a LEFT JOIN p.rules r " +
                "WHERE a.username = '" + username + "' OR r.global = true";
    }
}
