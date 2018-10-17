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

import io.galeb.api.dao.GenericDaoService;
import io.galeb.api.repository.custom.WithRoles;
import io.galeb.api.services.AuditService.AuditType;
import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.Account;
import io.galeb.core.entity.WithGlobal;
import java.util.Collections;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@SuppressWarnings("WeakerAccess, unused")
@Service("perm")
public class PermissionService {

    public enum Action {
        CREATE,
        UPDATE,
        DELETE,
        VIEW
    }

    @Autowired
    GenericDaoService genericDaoService;

    @Autowired
    private AuditService auditService;

    private static final Logger LOGGER = LogManager.getLogger(PermissionService.class);

    private boolean allow(Action action, Object criteria, MethodSecurityExpressionOperations expressionOperations) {
        Account account = (Account)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Object operationsThis = expressionOperations.getThis();
        if (criteria != null && operationsThis == null) {
            // 404
            return true;
        }
        WithRoles repository;
        if (operationsThis instanceof WithRoles) {
            repository = (WithRoles) operationsThis;
        } else {
            LOGGER.error("{} is not an instance of {}", operationsThis, WithRoles.class.getSimpleName());
            return false;
        }

        Set<String> roles = repository.mergeAllRolesOf(account);

        String classSimpleName = repository.classEntity().getSimpleName();
        String roleUpperCase = classSimpleName.toUpperCase() + "_" + action;

        boolean allow = false;
        AuditType audiType = AuditType.ROLE;
        boolean isAccount = criteria instanceof Account;
        Set<String> contextRoles = repository.roles(criteria);
        switch (action) {
            case CREATE:
                allow = isAccount ? ((Account) criteria).getId() == account.getId() : hasRoleSelf(roles, roleUpperCase);
                break;
            case UPDATE:
            case DELETE:
                allow = isAccount ? ((Account) criteria).getId() == account.getId() : hasContextRole(contextRoles, roleUpperCase);
                break;
            case VIEW:
                allow = criteria == null || hasGlobal(criteria, repository.classEntity()) || hasContextRole(contextRoles, roleUpperCase);
                break;

        }
        if (!allow) {
            allow = hasRoleAll(roles, roleUpperCase);
        }
        auditService.logAccess(roleUpperCase, roles, allow, classSimpleName, action.toString(), criteria, audiType);
        return allow;
    }

    public boolean allowSave(Object criteria, MethodSecurityExpressionOperations expressionOperations) {
        Action action = Action.UPDATE;
        if (criteria instanceof AbstractEntity) {
            AbstractEntity entity = (AbstractEntity) criteria;
            action = entity.getId() != 0 ? Action.UPDATE : Action.CREATE;
        }

        return allow(action, criteria, expressionOperations);
    }

    public boolean allowDelete(Object criteria, MethodSecurityExpressionOperations expressionOperations) {
        return allow(Action.DELETE, criteria, expressionOperations);
    }

    public boolean allowView(Object criteria, MethodSecurityExpressionOperations expressionOperations) {
        return allow(Action.VIEW, criteria, expressionOperations);
    }

    private boolean hasRoleSelf(Set<String> roles, String role) {
        return roles.stream().anyMatch(role::equals);
    }

    private boolean hasRoleAll(Set<String> roles, String role) {
        return roles.stream().anyMatch(r -> r.equals(role + "_ALL"));
    }

    private boolean hasGlobal(Object criteria, Class<? extends AbstractEntity> entityClass) {
        if (criteria instanceof Long && entityClass != null) {
            AbstractEntity entity = genericDaoService.findOne(entityClass, (Long) criteria);
            if (entity instanceof WithGlobal && ((WithGlobal) entity).getGlobal()) {
                String criteriaName = entityClass.getSimpleName();
                auditService.logAccess("", Collections.emptySet(), true, criteriaName, Action.VIEW.toString(), criteria, AuditType.GLOBAL);
                return true;
            }
        }
        return false;
    }

    private boolean hasContextRole(Set<String> roles, String role) {
        return roles != null && roles.stream().anyMatch(role::equals);
    }
}
