package io.galeb.api.services;

import io.galeb.api.repository.*;
import io.galeb.api.repository.custom.WithRoles;
import io.galeb.api.security.LocalAdmin;
import io.galeb.core.entity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

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

    public boolean allow(Object principal, Object criteria, MethodSecurityExpressionOperations expressionOperations, String action) {
        Class<? extends AbstractEntity> entityClass = extractEntityClass(expressionOperations);
        if (principal instanceof Account && entityClass != null) {
            String roleEntityPrefix = entityClass.getSimpleName().toUpperCase();
            String role = roleEntityPrefix + "_" + action;
            return  ((Account.class.equals(entityClass)) && isMySelf(principal, criteria)) ||
                    isLocalAdmin(principal) ||
                    isAdmin(principal) ||
                    hasRole(principal, role + "_ALL") ||
                    hasRole(principal, criteria, expressionOperations.getThis(), role);
        }
        return false;
    }

    private boolean hasRole(Object principal, Object criteria, Object repositoryObj, String role) {
        Account account = (Account) principal;
        WithRoles repository = null;
        if (repositoryObj instanceof WithRoles) {
            repository = (WithRoles) repositoryObj;
        }
        if (repository != null) {
            Set<String> roles = repository.roles(principal, criteria);
            boolean result = roles.stream().anyMatch(r -> r.equals(role));
            auditHasRole(role, account, roles, result);
            return result;
        }
        return false;
    }

    public boolean isAdmin(Object principal) {
        Account account = (Account) principal;
        return hasRole(account, RoleGroup.Role.ADMIN.toString());
    }

    public boolean hasRole(Object principal, String role) {
        Account account = (Account) principal;
        boolean result = account.getAuthorities().stream().anyMatch(a -> role.equals(a.getAuthority()));
        auditHasRole(role, account, account.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()), result);
        return result;
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
    }

    private void auditHasRole(String role, Account account, Set<String> roles, boolean result) {
        LOGGER.warn("AUDIT: " + account.getUsername() + " (roles: " + roles.stream().collect(Collectors.joining(",")) + ") has " + role + " = " + result);
    }

}