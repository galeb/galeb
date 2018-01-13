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

import io.galeb.api.repository.*;
import io.galeb.api.repository.custom.WithRoles;
import io.galeb.api.security.LocalAdmin;
import io.galeb.api.services.AuditService.AuditType;
import io.galeb.core.entity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess, unused")
@Service("perm")
public class PermissionService {

    public enum Action {
        SAVE,
        DELETE,
        VIEW
    }

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private AuditService auditService;

    private static final Logger LOGGER = LogManager.getLogger(PermissionService.class);

    public boolean allowSave(Object criteria, MethodSecurityExpressionOperations expressionOperations) {
        Account account = (Account)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return allow(account, criteria, expressionOperations, Action.SAVE.toString());
    }

    public boolean allowDelete(Object criteria, MethodSecurityExpressionOperations expressionOperations) {
        Account account = (Account)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return allow(account, criteria, expressionOperations, Action.DELETE.toString());
    }

    public boolean allowView(Object criteria, MethodSecurityExpressionOperations expressionOperations) {
        Account account = (Account)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (criteria == null) {
            Class<? extends AbstractEntity> entityClass = extractEntityClass(expressionOperations);
            if (entityClass == null) return false;
            String criteriaName = entityClass.getSimpleName();
            String realCriteria = "ALL";
            if (isAdmin(account, entityClass.getSimpleName(), Action.VIEW.toString(), realCriteria)) {
                return true;
            }
            Set<String> roles = mergeRoles(account);
            String roleView = criteriaName.toUpperCase() + "_VIEW";
            boolean isView = roles.contains(roleView);
            auditService.logAccess(roleView, roles, isView, criteriaName, Action.VIEW.toString(), realCriteria, AuditService.AuditType.ROLE);

            String roleViewAll = roleView + "_ALL";
            boolean isViewAll = roles.contains(roleViewAll);
            auditService.logAccess(roleViewAll, roles, isViewAll, criteriaName, Action.VIEW.toString(), realCriteria, AuditService.AuditType.ROLE);
            return true; // audit only
        }
        return allow(account, criteria, expressionOperations, Action.VIEW.toString());
    }

    @SuppressWarnings("unchecked")
    private boolean allow(Account account, Object criteria, MethodSecurityExpressionOperations expressionOperations, String action) {
        Class<? extends AbstractEntity> entityClass = extractEntityClass(expressionOperations);
        if (entityClass == null) return false;
        String roleEntityPrefix = entityClass.getSimpleName().toUpperCase();
        String role = roleEntityPrefix + "_" + action;
        final Object repositoryObj = expressionOperations.getThis();
        WithRoles repository;
        if (repositoryObj instanceof WithRoles) {
            repository = (WithRoles) repositoryObj;
        } else {
            LOGGER.error("{} is not an instance of {}", repositoryObj, WithRoles.class.getSimpleName());
            return false;
        }
        return  ((Account.class.equals(entityClass)) && !(criteria instanceof Account) && isMySelf(account, criteria, entityClass.getSimpleName(), action)) ||
                isAdmin(account, entityClass.getSimpleName(), action, criteria) ||
                hasSelfRole(account, role + "_ALL", entityClass.getSimpleName(), action, criteria) ||
                hasContextRole(criteria, repository, role, entityClass.getSimpleName(), action);
    }

    private boolean hasContextRole(Object criteria, Object repositoryObj, String role, String entityClass, String action) {
        WithRoles repository;
        if (repositoryObj instanceof WithRoles) {
            repository = (WithRoles) repositoryObj;
        } else {
            LOGGER.error("{} is not an instance of {}", repositoryObj, WithRoles.class.getSimpleName());
            return false;
        }
        Set<String> roles = repository.roles(criteria);
        boolean result = roles.stream().anyMatch(role::equals);
        auditService.logAccess(role, roles, result, entityClass, action, criteria, AuditType.ROLE);
        return result;
    }

    private boolean hasSelfRole(Account account, String role, String entityClass, String action, Object criteria) {
        Set<String> roles = mergeRoles(account);
        boolean result = roles.contains(role);
        auditService.logAccess(role, roles, result, entityClass, action, criteria, AuditType.ROLE);
        return result;
    }

    private Set<String> mergeRoles(Account account) {
        long accountId = account.getId();
        List<RoleGroup> roleGroupsFromTeams = em.createNamedQuery("roleGroupsFromTeams", RoleGroup.class)
                .setParameter("id", accountId)
                .getResultList();
        Set<String> roles = roleGroupsFromTeams.stream().flatMap(rg -> rg.getRoles().stream())
                .distinct().map(Enum::toString).distinct().collect(Collectors.toSet());
        List<RoleGroup> roleGroupsFromAccount = em.createNamedQuery("roleGroupsFromAccount", RoleGroup.class)
                .setParameter("id", accountId)
                .getResultList();
        roles.addAll(roleGroupsFromAccount.stream().flatMap(rg -> rg.getRoles().stream())
                .distinct().map(Enum::toString).collect(Collectors.toSet()));
        return roles;
    }

    private boolean isAdmin(Account account, String entityClass, String action, Object criteria) {
        if (hasSelfRole(account, Role.ADMIN.toString(), entityClass, action, criteria)) {
            return true;
        }
        boolean result = LocalAdmin.NAME.equals(account.getUsername());
        auditService.logAccess(null, Collections.emptySet(), result, entityClass, action, criteria,  AuditType.LOCAL_ADMIN);
        return result;
    }

    private boolean isMySelf(Account account, Object criteria, String entityClass, String action) {
        boolean result = false;
        if (criteria instanceof Account) {
            result = criteria.equals(account);
        }
        if (criteria instanceof Long) {
            Long id = (Long) criteria;
            result = account.getId() == id;
        }
        auditService.logAccess(null, Collections.emptySet(), result, entityClass, action, criteria, AuditType.MYSELF);
        return result;
    }

    private Class<? extends AbstractEntity> extractEntityClass(MethodSecurityExpressionOperations securityExpressionOperations) {
        Object o = securityExpressionOperations.getThis();
        // @formatter:off
        return  o instanceof AccountRepository ? Account.class :
                o instanceof BalancePolicyRepository ? BalancePolicy.class :
                o instanceof EnvironmentRepository ? Environment.class :
                o instanceof HealthCheckRepository ? HealthCheck.class :
                o instanceof HealthStatusRepository ? HealthStatus.class :
                o instanceof PoolRepository ? Pool.class :
                o instanceof ProjectRepository ? Project.class :
                o instanceof RoleGroupRepository ? RoleGroup.class :
                o instanceof RuleOrderedRepository ? RuleOrdered.class :
                o instanceof RuleRepository ? Rule.class :
                o instanceof TargetRepository ? Target.class :
                o instanceof TeamRepository ? Team.class :
                o instanceof VirtualhostGroupRepository ? VirtualhostGroup.class :
                o instanceof VirtualHostRepository ? VirtualHost.class : null;
        // @formatter:on
    }

}