package io.galeb.api.services;

import io.galeb.api.repository.*;
import io.galeb.api.security.LocalAdmin;
import io.galeb.core.entity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.stereotype.Service;

@Service("authz")
public class ConditionalAuthorizerService {

    private static final Logger LOGGER = LogManager.getLogger(ConditionalAuthorizerService.class);

    public boolean check(Object principal, MethodSecurityExpressionOperations securityExpressionOperations) {
        return check(principal, null, securityExpressionOperations);
    }

    public boolean check(Object principal, Object criteria, MethodSecurityExpressionOperations securityExpressionOperations) {
        Object jpaRepositoryObj = securityExpressionOperations.getThis();
        Class<? extends AbstractEntity> entityClass = extractType(jpaRepositoryObj);
        if (principal instanceof Account) {
            Account account = (Account) principal;
            if (LocalAdmin.NAME.equals(account.getUsername()) || account.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
                return true;
            }

            // TODO: for each Entity class, then ...
            if (Account.class.equals(entityClass)) {
                return checkAccount(account, criteria);
            }
        }

        return false;
    }

    private boolean checkAccount(Account account, Object criteria) {
        if (criteria instanceof Account) {
            return criteria.equals(account);
        }
        if (criteria instanceof Long) {
            Long id = (Long) criteria;
            return account.getId() == id;
        }
        return false;
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