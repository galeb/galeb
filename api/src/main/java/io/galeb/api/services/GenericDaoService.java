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
import io.galeb.core.entity.Project;
import io.galeb.core.entity.RoleGroup;
import io.galeb.core.entity.Team;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class GenericDaoService {

    @PersistenceContext
    private EntityManager em;

    public EntityManager entityManager() {
        return em;
    }

//    @Cacheable(value = "findOne", key = "{ #classEntity.name, #id }")
    public AbstractEntity findOne(Class<? extends AbstractEntity> classEntity, Long id) {
        return em.find(classEntity, id);
    }

//    @Cacheable(value = "findByName", key = "{ #classEntity.name, #name }")
    public AbstractEntity findByName(Class<? extends AbstractEntity> classEntity, String name) {
        return em.createQuery("SELECT e FROM " + classEntity.getSimpleName() + " e WHERE e.name = :name", classEntity)
            .setParameter("name", name)
            .getSingleResult();
    }

//    @Cacheable(value = "findAll", key = "{ #entityClass.name, #pageable }")
    public List<? extends AbstractEntity> findAll(Class<? extends AbstractEntity> entityClass, Pageable pageable) {
        TypedQuery<? extends AbstractEntity> query = em.createQuery("SELECT DISTINCT entity From " + entityClass.getSimpleName() + " entity", entityClass);
        query.setFirstResult(pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        return query.getResultList();
    }

//    @Cacheable(value = "findAllNamed", key = "{ #namedquery, #entityClass.name, #username, #pageable.offset, #pageable.pageNumber, #pageable.pageSize, #pageable }")
    public List<? extends AbstractEntity> findAllNamed(String namedquery, Class<? extends AbstractEntity> entityClass, String username, Pageable pageable) {
        TypedQuery<? extends AbstractEntity> query = em.createNamedQuery(namedquery, entityClass).setParameter("username", username);
        query.setFirstResult(pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        return query.getResultList();
    }

    @Cacheable(value = "mergeAllRolesOf", key = "#accountId")
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

    private List<RoleGroup> rolegroupsNamedQuery(String namedquery, Long id) {
        return em.createNamedQuery(namedquery, RoleGroup.class)
            .setParameter("id", id)
            .getResultList();
    }

//    @Cacheable(value = "projectsHealthStatus", key = "#id")
    public List<Project> projectsHealthStatus(Long id) {
        return projectsNamedQuery("projectHealthStatus", id);
    }

//    @Cacheable(value = "projectsFromRuleOrdered", key = "#id")
    public List<Project> projectsFromRuleOrdered(Long id) {
        return projectsNamedQuery("projectsFromRuleOrdered", id);
    }

//    @Cacheable(value = "projectsFromTarget", key = "#id")
    public List<Project> projectsFromTarget(Long id) {
        return projectsNamedQuery("projectFromTarget", id);
    }

//    @Cacheable(value = "projectFromVirtualhostGroup", key = "#id")
    public List<Project> projectFromVirtualhostGroup(Long id) {
        return projectsNamedQuery("projectFromVirtualhostGroup", id);
    }

    private List<Project> projectsNamedQuery(String namedquery, Long id) {
        return em.createNamedQuery(namedquery, Project.class)
            .setParameter("id", id)
            .getResultList();
    }

//    @Cacheable(value = "projectLinkedToAccount", key = "{ #accountId, #projectId }")
    public List<Project> projectLinkedToAccount(Long accountId, Long projectId) {
        return em.createNamedQuery("projectLinkedToAccount", Project.class)
            .setParameter("account_id", accountId)
            .setParameter("project_id", projectId).getResultList();
    }

//    @Cacheable(value = "roleGroupsFromProject", key = "{ #accountId, #projectId }")
    public List<RoleGroup> roleGroupsFromProject(Long accountId, Long projectId) {
        return em.createNamedQuery("roleGroupsFromProject", RoleGroup.class)
            .setParameter("account_id", accountId)
            .setParameter("project_id", projectId)
            .getResultList();
    }

//    @Cacheable(value = "teamLinkedToAccount", key = "{ #accountId, #teamId }")
    public List<Team> teamLinkedToAccount(Long accountId, Long teamId) {
        return em.createNamedQuery("teamLinkedToAccount", Team.class)
            .setParameter("account_id", accountId)
            .setParameter("team_id", teamId).getResultList();
    }

}
