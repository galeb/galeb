package io.galeb.api.services;

import io.galeb.api.repository.*;
import io.galeb.api.security.LocalAdmin;
import io.galeb.core.entity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.stereotype.Service;

@SuppressWarnings("Duplicates")
@Service("authz")
public class ConditionalAuthorizerService {

    private static final Logger LOGGER = LogManager.getLogger(ConditionalAuthorizerService.class);

    public boolean checkDelete(Object principal, Object criteria, MethodSecurityExpressionOperations securityExpressionOperations) {
        Class<? extends AbstractEntity> entityClass = extractEntityClass(securityExpressionOperations);
        if (principal instanceof Account) {

            // TODO: for each Entity class, then ...
            if (Account.class.equals(entityClass)) {
                return accountPermission(principal, criteria);
            }
            //

            if (isAdmin(principal)) return true;
        }
        return false;
    }

    public boolean checkSave(Object principal, Object criteria, MethodSecurityExpressionOperations securityExpressionOperations) {
        Class<? extends AbstractEntity> entityClass = extractEntityClass(securityExpressionOperations);
        if (principal instanceof Account) {

            // TODO: for each Entity class, then ...
            if (Account.class.equals(entityClass)) {
                return accountPermission(principal, criteria);
            }
            //

            if (isAdmin(principal)) return true;
        }
        return false;
    }

    public boolean isAdmin(Object principal) {
        Account account = (Account) principal;
        return isLocalAdmin(account) || account.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
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

    private boolean accountPermission(Object principal, Object criteria) {
        return isAdmin(principal) || isMySelf(principal, criteria);
    }

    private Class<? extends AbstractEntity> extractEntityClass(MethodSecurityExpressionOperations securityExpressionOperations) {
        Object jpaRepositoryObj = securityExpressionOperations.getThis();
        return extractType(jpaRepositoryObj);
    }

    private Class<? extends AbstractEntity> extractType(Object o) {
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