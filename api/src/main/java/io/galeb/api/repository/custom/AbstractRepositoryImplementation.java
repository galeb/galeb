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

import com.google.common.reflect.TypeToken;
import io.galeb.api.services.LocalAdminService;
import io.galeb.api.services.StatusService;
import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.Account;
import io.galeb.core.entity.Environment;
import io.galeb.core.entity.Project;
import io.galeb.core.entity.RoleGroup;
import io.galeb.core.entity.WithStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.galeb.api.services.PermissionService.Action.VIEW;

@SuppressWarnings("unused")
@NoRepositoryBean
public abstract class AbstractRepositoryImplementation<T extends AbstractEntity> implements WithRoles {

    private static final Logger LOGGER = LogManager.getLogger(AbstractRepositoryImplementation.class);

    Long NOT_FOUND = -404L;

    private SimpleJpaRepository<T, Long> simpleJpaRepository;
    private StatusService statusService;
    private EntityManager em;
    private TypeToken<T> typeToken = new TypeToken<T>(getClass()) {};
    private Class<? super T> entityClass = typeToken.getRawType();

    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
    }

    public void setSimpleJpaRepository(Class<T> klazz, EntityManager entityManager) {
        if (this.simpleJpaRepository != null) return;
        this.simpleJpaRepository = new SimpleJpaRepository<>(klazz, entityManager);
        this.em = entityManager;
    }

    public T findOne(Long id) {
        T entity = simpleJpaRepository.findOne(id);
        setStatus(entity);
        return entity;
    }

    @Deprecated
    public Iterable<T> findAll(Sort sort) {
        LOGGER.warn("Sorting not yet supported");
        return Collections.emptySet();
    }

    @Transactional
    public T saveByPass(T entity) {
        return simpleJpaRepository.saveAndFlush(entity);
    }


    @SuppressWarnings("unchecked")
    public Page<T> findAll(Pageable pageable) {
        Account account = (Account)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isViewAll;
        boolean isView = false;
        if (LocalAdminService.NAME.equals(account.getUsername())) {
            isViewAll = true;
        } else {
            Set<String> roles = mergeAllRolesOf(account);
            String roleView = entityClass.getSimpleName().toUpperCase() + "_VIEW";
            isView = roles.contains(roleView);

            String roleViewAll = roleView + "_ALL";
            isViewAll = roles.contains(roleViewAll);
        }

        if (!isView && !isViewAll) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        String username = account.getUsername();
        TypedQuery<?> query = em.createQuery(isViewAll ? selectPrefix() : selectPrefix() + " " + querySuffix(username), entityClass);
        TypedQuery<Long> queryCount = em.createQuery(isViewAll ? selectCountPrefix() : selectCountPrefix() + " " + querySuffix(username), Long.class);

        query.setFirstResult(pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<?> queryResult = query.getResultList();
        Page<T> page = new PageImpl<>((List<T>) queryResult, pageable, queryCount.getSingleResult());
        for (T entity: page) {
            setStatus(entity);
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

    protected Set<Environment> getAllEnvironments(AbstractEntity entity) {
        return Collections.emptySet();
    }

    protected long getProjectId(Object criteria) {
        return -1;
    }

    private Set<String> projectRoles(Account account, long projectId) {
        try {
            if (!isAccountLinkedWithProject(account.getId(), projectId)) return Collections.emptySet();
            return mergeAllRolesOf(account);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return Collections.emptySet();
    }

    private boolean isAccountLinkedWithProject(long accountId, long projectId) {
        List<Project> projects = em.createNamedQuery("projectLinkedToAccount", Project.class)
                .setParameter("account_id", accountId)
                .setParameter("project_id", projectId).getResultList();
        if (projects == null || projects.isEmpty()) {
            LOGGER.warn("Project id " + projectId + " not linked with account id " + accountId);
            return false;
        }
        return true;
    }

    protected Set<String> mergeRoles(long projectId) {
        Account account = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long accountId = account.getId();
        List<RoleGroup> roleGroupsFromProject = new ArrayList<>();
        if (projectId != -1) {
            roleGroupsFromProject = em.createNamedQuery("roleGroupsFromProject", RoleGroup.class)
                    .setParameter("account_id", accountId)
                    .setParameter("project_id", projectId)
                    .getResultList();
        }
        return roleGroupsFromProject.stream().flatMap(rg -> rg.getRoles().stream()).distinct().map(Enum::toString).collect(Collectors.toSet());
    }

    @Override
    public Set<String> mergeAllRolesOf(Account account) {
        long accountId = account.getId();
        List<RoleGroup> roleGroupsFromTeams = em.createNamedQuery("roleGroupsFromTeams", RoleGroup.class)
                .setParameter("id", accountId)
                .getResultList();
        Set<String> roles = roleGroupsFromTeams.stream().flatMap(rg -> rg.getRoles().stream())
                .distinct().map(Enum::toString).collect(Collectors.toSet());
        List<RoleGroup> roleGroupsFromAccount = em.createNamedQuery("roleGroupsFromAccount", RoleGroup.class)
                .setParameter("id", accountId)
                .getResultList();
        roles.addAll(roleGroupsFromAccount.stream().flatMap(rg -> rg.getRoles().stream())
                .distinct().map(Enum::toString).collect(Collectors.toSet()));
        List<RoleGroup> roleGroupsFromProject = em.createNamedQuery("roleGroupsFromProjectByAccountId", RoleGroup.class)
                .setParameter("id", accountId)
                .getResultList();
        roles.addAll(roleGroupsFromProject.stream().flatMap(rg -> rg.getRoles().stream())
                .distinct().map(Enum::toString).collect(Collectors.toSet()));
        return roles;
    }

    public Set<String> roles(Object criteria) {
        Account account = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long projectId = getProjectId(criteria);
        if (projectId == NOT_FOUND) {
            return Collections.singleton(entityClass.getSimpleName().toUpperCase() + "_" + VIEW.toString());
        }
        if (projectId > -1) {
            long accountId = account.getId();
            return projectRoles(account, projectId);
        }
        return Collections.emptySet();
    }

    private void setStatus(T entity) {
        if (entity instanceof WithStatus) {
            entity.setAllEnvironments(getAllEnvironments(entity));
            ((WithStatus) entity).setStatus(statusService.status(entity));
        }
    }

    protected abstract String querySuffix(String username);

    protected String selectPrefix() {
        return "SELECT DISTINCT entity From " + entityClass.getSimpleName() + " entity";
    }

    private String selectCountPrefix() {
        return "SELECT COUNT(entity) From " + entityClass.getSimpleName() + " entity";
    }

    @Override
    public Class<? extends AbstractEntity> classEntity() {
        return (Class<? extends AbstractEntity>)entityClass;
    }

}
