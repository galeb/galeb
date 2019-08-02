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

import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.Account;
import io.galeb.core.entity.HealthStatus;
import io.galeb.core.entity.Project;
import io.galeb.core.entity.RoleGroup;
import io.galeb.core.entity.RuleOrdered;
import io.galeb.core.entity.Team;

@Service
public class GenericDaoService {

    private static final Logger LOGGER = LogManager.getLogger(GenericDaoService.class);

    @PersistenceContext
    private EntityManager em;

    public EntityManager entityManager() {
        return em;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public BigInteger numAccounts() {
        try {
            return (BigInteger) em.createNativeQuery("SELECT COUNT(*) FROM account").getSingleResult();
        } catch (NoResultException ignore) {
            return BigInteger.ZERO;
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public AbstractEntity findOne(Class<? extends AbstractEntity> classEntity, Long id) {
        return em.find(classEntity, id);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public AbstractEntity findByName(Class<? extends AbstractEntity> classEntity, String name) {
        String selectByName = "SELECT e FROM " + classEntity.getSimpleName();
        if (Account.class.equals(classEntity)) {
            selectByName = selectByName + " e WHERE e.username = :name";
        } else {
            selectByName = selectByName + " e WHERE e.name = :name";
        }
        return em.createQuery(selectByName, classEntity)
            .setParameter("name", name)
            .getSingleResult();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<? extends AbstractEntity> findAll(Class<? extends AbstractEntity> entityClass, Pageable pageable) {
        TypedQuery<? extends AbstractEntity> query = em.createQuery("SELECT DISTINCT entity From " + entityClass.getSimpleName() + " entity", entityClass);
        query.setFirstResult(pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        // TODO: Sorting?
        return query.getResultList();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<? extends AbstractEntity> findAllNamed(String namedquery, Class<? extends AbstractEntity> entityClass, String username, Pageable pageable) {
        TypedQuery<? extends AbstractEntity> query = em.createNamedQuery(namedquery, entityClass).setParameter("username", username);
        query.setFirstResult(pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        // TODO: Sorting?
        return query.getResultList();
    }

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

    public List<Project> projectFromHealthStatus(Long id) {
        return projectsNamedQuery("projectFromHealthStatus", id);
    }

    public List<Project> projectFromRuleOrdered(Long id) {
        return projectsNamedQuery("projectFromRuleOrdered", id);
    }

    public List<Project> projectFromTarget(Long id) {
        return projectsNamedQuery("projectFromTarget", id);
    }

    public List<Project> projectFromVirtualhostGroup(Long id) {
        return projectsNamedQuery("projectFromVirtualhostGroup", id);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Project> projectsNamedQuery(String namedquery, Long id) {
        return em.createNamedQuery(namedquery, Project.class)
            .setParameter("id", id)
            .getResultList();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Project> projectLinkedToAccount(Long accountId, Long projectId) {
        return em.createNamedQuery("projectLinkedToAccount", Project.class)
            .setParameter("account_id", accountId)
            .setParameter("project_id", projectId).getResultList();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<RoleGroup> roleGroupsFromProject(Long accountId, Long projectId) {
        return em.createNamedQuery("roleGroupsFromProject", RoleGroup.class)
            .setParameter("account_id", accountId)
            .setParameter("project_id", projectId)
            .getResultList();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Team> teamLinkedToAccount(Long accountId, Long teamId) {
        return em.createNamedQuery("teamLinkedToAccount", Team.class)
            .setParameter("account_id", accountId)
            .setParameter("team_id", teamId).getResultList();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Account findAccount(String username) {
        Account accountPersisted = null;
        try {
            accountPersisted = em.createQuery("SELECT a FROM Account a WHERE a.username = :username", Account.class)
                    .setParameter("username", username).getSingleResult();
        } catch (NoResultException ignored) {}
        return accountPersisted;
    }

    public boolean exist(String entityName, Long id) {
        try {
            final Query query = em.createNativeQuery("SELECT e.id FROM " + nativeTable(entityName) + " e WHERE e.id = :id").setParameter("id", id);
            return query.getSingleResult() != null;
        } catch (NoResultException ignore) {
            // NOT_FOUND
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean isGlobal(String entityName, Long id) {
        try {
            final Query query = getQuery(entityName, id);
            final Object result = query.getSingleResult();
            return result != null && (boolean) result;
        } catch (NoResultException ignore) {
            // NOT_FOUND
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    private Query getQuery(String entityName, Long id) {
        final StringBuilder fullquery = new StringBuilder();
        if (RuleOrdered.class.getSimpleName().equals(entityName)) {
            fullquery.append("SELECT r.global FROM ruleordered ro ")
                     .append("INNER JOIN rule r ON ro.rule_ruleordered_id = r.id ")
                     .append("WHERE ro.id = :id");
        } else {
            fullquery.append("SELECT e.global FROM ").append(nativeTable(entityName)).append(" e ")
                     .append("WHERE e.id = :id");
        }
        return em.createNativeQuery(fullquery.toString()).setParameter("id", id);
    }

    private String nativeTable(String entityName) {
        return HealthStatus.class.getSimpleName().equals(entityName) ? "health_status" : entityName.toLowerCase();
    }
}
