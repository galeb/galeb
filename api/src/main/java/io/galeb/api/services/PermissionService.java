package io.galeb.api.services;

import io.galeb.api.repository.*;
import io.galeb.api.repository.custom.WithRoles;
import io.galeb.api.security.LocalAdmin;
import io.galeb.core.entity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
@Service("perm")
public class PermissionService {

    private enum Action {
        SAVE,
        DELETE,
        VIEW
    }

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private AccountDaoService accountDaoService;

    private static final Logger LOGGER = LogManager.getLogger(PermissionService.class);

    public boolean allowSave(Object principal, Object criteria, MethodSecurityExpressionOperations expressionOperations) {
        return allow(principal, criteria, expressionOperations, Action.SAVE.toString());
    }

    public boolean allowDelete(Object principal, Object criteria, MethodSecurityExpressionOperations expressionOperations) {
        return allow(principal, criteria, expressionOperations, Action.DELETE.toString());
    }

    public boolean allowView(Object principal, Object criteria, MethodSecurityExpressionOperations expressionOperations) {
        return allow(principal, criteria, expressionOperations, Action.VIEW.toString());
    }

    @SuppressWarnings("unchecked")
    public boolean allow(Object principal, Object criteria, MethodSecurityExpressionOperations expressionOperations, String action) {
        Class<? extends AbstractEntity> entityClass = extractEntityClass(expressionOperations);
        if (principal instanceof Account && entityClass != null) {
            String roleEntityPrefix = entityClass.getSimpleName().toUpperCase();
            String role = roleEntityPrefix + "_" + action;
            final Object repositoryObj = expressionOperations.getThis();
            WithRoles repository;
            if (repositoryObj instanceof WithRoles) {
                repository = (WithRoles) repositoryObj;
            } else {
                LOGGER.error(repositoryObj + " is not instance of " + WithRoles.class.getSimpleName());
                return false;
            }
            return  ((Account.class.equals(entityClass)) && isMySelf(principal, criteria)) ||
                    isLocalAdmin(principal) ||
                    isAdmin(principal) ||
                    hasRole(principal, role + "_ALL") ||
                    hasRole(principal, criteria, repository, role);
        }
        return false;
    }

    private boolean hasRole(Object principal, Object criteria, Object repositoryObj, String role) {
        Account account = (Account) principal;
        WithRoles repository;
        if (repositoryObj instanceof WithRoles) {
            repository = (WithRoles) repositoryObj;
        } else {
            LOGGER.error(repositoryObj + " is not instance of " + WithRoles.class.getSimpleName());
            return false;
        }
        Set<String> roles = repository.roles(principal, criteria);
        return hasRole(account, role, roles);
    }

    private boolean hasRole(Object principal, String role) {
        Account account = (Account) principal;
        long accountId = account.getId();
        Set<String> roles = new HashSet<>();
//        List<RoleGroup> roleGroupsFromTeams = em.createNamedQuery("roleGroupsFromTeams", RoleGroup.class)
//                .setParameter("id", accountId)
//                .getResultList();
//        Set<String> roles = roleGroupsFromTeams.stream().flatMap(rg -> rg.getRoles().stream()).distinct().map(Enum::toString).distinct().collect(Collectors.toSet());
        List<RoleGroup> roleGroupsFromAccount = em.createNamedQuery("roleGroupsFromAccount", RoleGroup.class)
                .setParameter("id", accountId)
                .getResultList();
        roles.addAll(roleGroupsFromAccount.stream().flatMap(rg -> rg.getRoles().stream()).distinct().map(Enum::toString).collect(Collectors.toSet()));

        boolean result = roles.contains(role);
        audit(account, role, roles, result);
        return result;
    }

    private boolean hasRole(Account account, String role, Set<String> roles) {
        boolean result = roles.stream().anyMatch(role::equals);
        audit(account, role, roles, result);
        return result;
    }

    public boolean isAdmin(Object principal) {
        Account account = (Account) principal;
        return hasRole(account, Role.ADMIN.toString());
    }

    public boolean isLocalAdmin(Object principal) {
        Account account = (Account) principal;
        boolean result = LocalAdmin.NAME.equals(account.getUsername());
        LOGGER.warn("AUDIT: Is Local Admin? " + result);
        return result;
    }

    public boolean isMySelf(Object principal, Object criteria) {
        Account account = (Account) principal;
        boolean result = false;
        if (criteria instanceof Account) {
            result = criteria.equals(account);
        }
        if (criteria instanceof Long) {
            Long id = (Long) criteria;
            result = account.getId() == id;
        }
        LOGGER.warn("AUDIT: Is my self? " + result);
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
                o instanceof RuleOrderedRepository ? RuleOrdered.class :
                o instanceof RuleRepository ? Rule.class :
                o instanceof TargetRepository ? Target.class :
                o instanceof TeamRepository ? Team.class :
                o instanceof VirtualhostGroupRepository ? VirtualhostGroup.class :
                o instanceof VirtualHostRepository ? VirtualHost.class : null;
        // @formatter:on
    }

    private void audit(Account account, String role, Set<String> roles, boolean result) {
        LOGGER.warn("AUDIT: " + account.getUsername() + " (roles: " + roles.stream().collect(Collectors.joining(",")) + ") has " + role + " = " + result);
    }

}