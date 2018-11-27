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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuditService {

    private static final Logger LOGGER = LogManager.getLogger(AuditService.class);

    @SuppressWarnings("unused")
    public enum AuditType {
        MYSELF      ("is myself?"),
        LOCAL_ADMIN ("is LocalAdmin?"),
        ROLE        ("has role "),
        GLOBAL      ("# Entity is global?");

        public String getMsg() {
            return msg;
        }

        private final String msg;
        AuditType(String msg) {
            this.msg = msg;
        }
    }

    @Value("${auth.show_roles:false}")
    private boolean showRoles;

    public void logAccess(String role, Set<String> roles, boolean result, String entityClass, String action, Object criteria, AuditType auditType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object detailsObj = authentication.getDetails();
        String remoteAddr = null;
        Account account = (Account) authentication.getPrincipal();
        if (detailsObj instanceof WebAuthenticationDetails) {
            remoteAddr = ((WebAuthenticationDetails) detailsObj).getRemoteAddress();
        }
        if (detailsObj instanceof OAuth2AuthenticationDetails) {
            remoteAddr = ((OAuth2AuthenticationDetails) detailsObj).getRemoteAddress();
        }
        register(String.format("[%s/%s/%s]: %s%s %s %s",
                entityClass,
                action,
                criteria instanceof AbstractEntity ? ((AbstractEntity)criteria).getId() : criteria,
                account.getUsername() + (remoteAddr != null ? "/" + remoteAddr : ""),
                showRoles ? " (roles: " + String.join(",", roles) + ")" : "",
                auditType == AuditType.ROLE ? auditType.getMsg() + role + "?" : auditType.getMsg(),
                result));
    }

    public void register(String msg) {
        LOGGER.warn(msg);
    }
}
