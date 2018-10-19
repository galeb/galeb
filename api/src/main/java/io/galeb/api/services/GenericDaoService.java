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

package io.galeb.api.services;

import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.Account;
import io.galeb.core.entity.Project;
import io.galeb.core.entity.RoleGroup;
import io.galeb.core.entity.Team;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GenericDaoService {

    @PersistenceContext
    private EntityManager em;

    public EntityManager entityManager() {
        return em;
    }

    @Cacheable(value = "cache_findOneDao", unless = "#result == null", key = "{ #root.methodName, #p0.name, #p1 }")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public AbstractEntity findOne(Class<? extends AbstractEntity> classEntity, Long id) {
        return em.find(classEntity, id);
    }

    @Cacheable(value = "cache_findByNameDao", unless = "#result == null", key = "{ #root.methodName, #p0.name, #p1 }")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public AbstractEntity findByName(Class<? extends AbstractEntity> classEntity, String name) {
        return em.createQuery("SELECT e FROM " + classEntity.getSimpleName() + " e WHERE e.name = :name", classEntity)
            .setParameter("name", name)
            .getSingleResult();
    }

    @Cacheable(value = "cache_findAllDao", unless = "#result == null or #result?.empty", key = "{ #root.methodName, #p0.name, #p1?.hashCode() }")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<? extends AbstractEntity> findAll(Class<? extends AbstractEntity> entityClass, Pageable pageable) {
        TypedQuery<? extends AbstractEntity> query = em.createQuery("SELECT DISTINCT entity From " + entityClass.getSimpleName() + " entity", entityClass);
        query.setFirstResult(pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        // TODO: Sorting?
        return query.getResultList();
    }

    @Cacheable(value = "cache_findAllNamedDao", unless = "#result == null or #result?.empty", key = "{ #root.methodName, #p0, #p1.name, #p2, #p3?.hashCode() }")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<? extends AbstractEntity> findAllNamed(String namedquery, Class<? extends AbstractEntity> entityClass, String username, Pageable pageable) {
        TypedQuery<? extends AbstractEntity> query = em.createNamedQuery(namedquery, entityClass).setParameter("username", username);
        query.setFirstResult(pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        // TODO: Sorting?
        return query.getResultList();
    }

    @Cacheable(value = "cache_mergeAllRolesOfDao", unless = "#result == null or #result?.empty", key = "{ #root.methodName, #p0 }")
    public Set<String> mergeAllRolesOf(Long accountId) {
        //TODO: INNER JOIN roleGroupsFromTeams, roleGroupsFromAccount & roleGroupsFromProjectByAccountId ?

        List<RoleGroup> roleGroupsFromTeams = roleGroupsFromTeams(accountId);
        Set<String> roles = roleGroupsFromTeams.stream().flatMap(rg -> rg.getRoles().stream())
                .map(Enum::toString).collect(Collectors.toSet());
        List<RoleGroup> roleGroupsFromAccount = roleGroupsFromAccount(accountId);
        roles.addAll(roleGroupsFromAccount.stream().flatMap(rg -> rg.getRoles().stream())
                .map(Enum::toString).collect(Collectors.toSet()));
        List<RoleGroup> roleGroupsFromProject = roleGroupsFromProjectByAccountId(accountId);
        roles.addAll(roleGroupsFromProject.stream().flatMap(rg -> rg.getRoles().stream())
                .map(Enum::toString).collect(Collectors.toSet()));
        return roles;
    }

    private List<RoleGroup> roleGroupsFromTeams(Long accountId) {
        return rolegroupsNamedQuery("roleGroupsFromTeams", accountId);
    }

    private List<RoleGroup> roleGroupsFromAccount(Long accountId) {
        return rolegroupsNamedQuery("roleGroupsFromAccount", accountId);
    }

    private List<RoleGroup> roleGroupsFromProjectByAccountId(Long accountId) {
        return rolegroupsNamedQuery("roleGroupsFromProjectByAccountId", accountId);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<RoleGroup> rolegroupsNamedQuery(String namedquery, Long id) {
        return em.createNamedQuery(namedquery, RoleGroup.class)
            .setParameter("id", id)
            .getResultList();
    }

    @Cacheable(value = "cache_projectFromHealthStatusDao", unless = "#result == null or #result?.empty", key = "{ #root.methodName, #p0 }")
    public List<Project> projectFromHealthStatus(Long id) {
        return projectsNamedQuery("projectFromHealthStatus", id);
    }

    @Cacheable(value = "cache_projectFromRuleOrderedDao", unless = "#result == null or #result?.empty", key = "{ #root.methodName, #p0 }")
    public List<Project> projectFromRuleOrdered(Long id) {
        return projectsNamedQuery("projectFromRuleOrdered", id);
    }

    @Cacheable(value = "cache_projectFromTargetDao", unless = "#result == null or #result?.empty", key = "{ #root.methodName, #p0 }")
    public List<Project> projectFromTarget(Long id) {
        return projectsNamedQuery("projectFromTarget", id);
    }

    @Cacheable(value = "cache_projectFromVirtualhostGroupDao", unless = "#result == null or #result?.empty", key = "{ #root.methodName, #p0 }")
    public List<Project> projectFromVirtualhostGroup(Long id) {
        return projectsNamedQuery("projectFromVirtualhostGroup", id);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Project> projectsNamedQuery(String namedquery, Long id) {
        return em.createNamedQuery(namedquery, Project.class)
            .setParameter("id", id)
            .getResultList();
    }

    @Cacheable(value = "cache_projectLinkedToAccount", unless = "#result == null or #result?.empty", key = "{ #root.methodName, #p0, #p1 }")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Project> projectLinkedToAccount(Long accountId, Long projectId) {
        return em.createNamedQuery("projectLinkedToAccount", Project.class)
            .setParameter("account_id", accountId)
            .setParameter("project_id", projectId).getResultList();
    }

//    @Cacheable(value = "cache_roleGroupsFromProject", unless = "#result == null or #result?.empty", key = "{ #root.methodName, #p0, #p1 }")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<RoleGroup> roleGroupsFromProject(Long accountId, Long projectId) {
        return em.createNamedQuery("roleGroupsFromProject", RoleGroup.class)
            .setParameter("account_id", accountId)
            .setParameter("project_id", projectId)
            .getResultList();
    }

//    @Cacheable(value = "cache_teamLinkedToAccount", unless = "#result == null or #result?.empty", key = "{ #root.methodName, #p0, #p1 }")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Team> teamLinkedToAccount(Long accountId, Long teamId) {
        return em.createNamedQuery("teamLinkedToAccount", Team.class)
            .setParameter("account_id", accountId)
            .setParameter("team_id", teamId).getResultList();
    }

    @Cacheable(value = "cache_userDetailsDao", unless = "#result == null", key = "{ #root.methodName, #p0 }")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Account findAccount(String username) {
        Account accountPersisted = null;
        try {
            accountPersisted = em.createQuery("SELECT a FROM Account a WHERE a.username = :username", Account.class)
                    .setParameter("username", username).getSingleResult();
        } catch (NoResultException ignored) {}
        return accountPersisted;
    }

}
