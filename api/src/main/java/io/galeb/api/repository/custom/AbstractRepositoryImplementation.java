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
import io.galeb.core.entity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@NoRepositoryBean
public abstract class AbstractRepositoryImplementation<T extends AbstractEntity> implements WithRoles {

    private static final Logger LOGGER = LogManager.getLogger(AbstractRepositoryImplementation.class);

    private SimpleJpaRepository<T, Long> simpleJpaRepository;
    private StatusService statusService;
    private EntityManager em;

    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
    }

    public void setSimpleJpaRepository(Class<T> klazz, EntityManager entityManager) {
        if (this.simpleJpaRepository != null) return;
        this.simpleJpaRepository = new SimpleJpaRepository<>(klazz, entityManager);
    }

    public T findOne(Long id) {
        T entity = simpleJpaRepository.findOne(id);
        if (entity instanceof WithStatus) {
            entity.setAllEnvironments(getAllEnvironments(entity));
            ((WithStatus) entity).setStatus(statusService.status(entity));
        }
        return entity;
    }

    public Iterable<T> findAll(Sort sort) {
        Iterable<T> iterable = simpleJpaRepository.findAll(sort);
        for (T entity: iterable) {
            if (entity instanceof WithStatus) {
                entity.setAllEnvironments(getAllEnvironments(entity));
                ((WithStatus) entity).setStatus(statusService.status(entity));
            }
        }
        return iterable;
    }

    public Page<T> findAll(Pageable pageable) {
        Page<T> page = simpleJpaRepository.findAll(pageable);
        for (T entity: page) {
            if (entity instanceof WithStatus) {
                entity.setAllEnvironments(getAllEnvironments(entity));
                ((WithStatus) entity).setStatus(statusService.status(entity));
            }
        }
        return page;
    }

    @Transactional
    public void delete(Long id) {
        T entity = simpleJpaRepository.findOne(id);
        if (entity instanceof WithStatus) {
            entity.quarantine(true);
            simpleJpaRepository.saveAndFlush(entity);
        } else {
            simpleJpaRepository.delete(entity);
        }
    }

    protected void setEntityManager(EntityManager em) {
        this.em = em;
    }

    protected Set<Environment> getAllEnvironments(AbstractEntity entity) {
        return Collections.emptySet();
    }

    protected long getProjectId(Object criteria) {
        return -1;
    }

    private Set<String> projectRoles(long accountId, long projectId, final EntityManager em) {
        Set<String> roles;
        try {
            List<Project> projects = em.createNamedQuery("projectLinkedToAccount", Project.class)
                    .setParameter("account_id", accountId)
                    .setParameter("project_id", projectId).getResultList();
            if (projects == null || projects.isEmpty()) {
                LOGGER.warn("Project id " + projectId + " not linked with account id " + accountId);
                return Collections.emptySet();
            }
            roles = mergeRoles(accountId, projectId, em);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return Collections.emptySet();
        }
        return roles;
    }

    private Set<String> mergeRoles(long accountId, long projectId, EntityManager em) {
        List<RoleGroup> roleGroupsFromProject = em.createNamedQuery("roleGroupsFromProject", RoleGroup.class)
                .setParameter("account_id", accountId)
                .setParameter("project_id", projectId)
                .getResultList();
        Set<String> roles = roleGroupsFromProject.stream().flatMap(rg -> rg.getRoles().stream()).distinct().map(Enum::toString).collect(Collectors.toSet());
        List<RoleGroup> roleGroupsFromTeams = em.createNamedQuery("roleGroupsFromTeams", RoleGroup.class)
                .setParameter("id", accountId)
                .getResultList();
        roles.addAll(roleGroupsFromTeams.stream().flatMap(rg -> rg.getRoles().stream())
                .distinct().map(Enum::toString).distinct().collect(Collectors.toSet()));
        List<RoleGroup> roleGroupsFromAccount = em.createNamedQuery("roleGroupsFromAccount", RoleGroup.class)
                .setParameter("id", accountId)
                .getResultList();
        roles.addAll(roleGroupsFromAccount.stream().flatMap(rg -> rg.getRoles().stream())
                .distinct().map(Enum::toString).collect(Collectors.toSet()));
        return roles;
    }

    public Set<String> roles(Object principal, Object criteria) {
        Account account = (Account) principal;
        long projectId = getProjectId(criteria);
        if (projectId > -1) {
            long accountId = account.getId();
            return projectRoles(accountId, projectId, em);
        }
        return Collections.emptySet();
    }

}
