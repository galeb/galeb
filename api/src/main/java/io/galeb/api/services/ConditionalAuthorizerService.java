package io.galeb.api.services;

import io.galeb.api.repository.*;
import io.galeb.api.repository.custom.WithRoles;
import io.galeb.api.security.LocalAdmin;
import io.galeb.core.entity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.stereotype.Service;

@Service("authz")
public class ConditionalAuthorizerService {

    private enum Action {
        SAVE,
        DELETE,
        VIEW
    }

    private static final Logger LOGGER = LogManager.getLogger(ConditionalAuthorizerService.class);

    public boolean checkSave(Object principal, Object criteria, MethodSecurityExpressionOperations securityExpressionOperations) {
        return check(principal, criteria, securityExpressionOperations, Action.SAVE);
    }

    public boolean checkDelete(Object principal, Object criteria, MethodSecurityExpressionOperations securityExpressionOperations) {
        return check(principal, criteria, securityExpressionOperations, Action.DELETE);
    }

    public boolean check(Object principal, Object criteria, MethodSecurityExpressionOperations securityExpressionOperations, Action action) {
        Class<? extends AbstractEntity> entityClass = extractEntityClass(securityExpressionOperations);
        if (principal instanceof Account && entityClass != null) {
            if (Account.class.equals(entityClass)) return isMySelf(principal, criteria);
            String roleEntityPrefix = entityClass.getName().toUpperCase();
            String role = roleEntityPrefix + "_" + action;
            return  isAdmin(principal) ||
                    hasRole(principal, role + "_ALL") ||
                    hasRole(principal, criteria, securityExpressionOperations.getThis(), role);
        }
        return false;
    }

    private boolean hasRole(Object principal, Object criteria, Object repositoryObj, String role) {
        WithRoles repository = null;
        if (repositoryObj instanceof WithRoles) {
            repository = (WithRoles) repositoryObj;
        }
        if (repository != null) {
            boolean result = repository.hasPermission(principal, criteria, role);
            LOGGER.warn(repository + "/" + criteria + "/" + role + ": " + result);
            return result;
        }
        return false;
    }

    public boolean isAdmin(Object principal) {
        Account account = (Account) principal;
        return isLocalAdmin(account) || hasRole(account, "ADMIN");
    }

    public boolean hasRole(Object principal, String role) {
        Account account = (Account) principal;
        return account.getAuthorities().stream().anyMatch(a -> ("ROLE_" + role).equals(a.getAuthority()));
    }

    public boolean isLocalAdmin(Object principal) {
        Account account = (Account) principal;
        return LocalAdmin.NAME.equals(account.getUsername());
    }

    public boolean isMySelf(Object principal, Object criteria) {
        Account account = (Account) principal;
        if (criteria instanceof Account) {
            return criteria.equals(account);
        }
        if (criteria instanceof Long) {
            Long id = (Long) criteria;
            return account.getId() == id;
        }
        return false;
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

}