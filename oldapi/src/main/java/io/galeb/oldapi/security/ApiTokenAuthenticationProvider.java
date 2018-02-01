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

package io.galeb.oldapi.security;

import io.galeb.oldapi.services.sec.LocalAdminService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class ApiTokenAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private static final Logger LOGGER = LogManager.getLogger(ApiTokenAuthenticationProvider.class);

    private final CurrentUserDetailsService currentUserDetailsService;
    private final LdapAuthenticationService ldapAuthenticationService;
    private final LocalAdminService localAdmin;

    @Autowired
    public ApiTokenAuthenticationProvider(CurrentUserDetailsService currentUserDetailsService,
                                          LdapAuthenticationService ldapAuthenticationService,
                                          LocalAdminService localAdmin) {
        this.currentUserDetailsService = currentUserDetailsService;
        this.ldapAuthenticationService = ldapAuthenticationService;
        this.localAdmin = localAdmin;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {

    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication.getPrincipal() == null) {
            String errMsg = "principal is NULL";
            LOGGER.error(errMsg);
            throw new SecurityException(errMsg);
        }
        boolean isAdmin = LocalAdminService.NAME.equals(authentication.getName()) && localAdmin.check((String) authentication.getCredentials());
        if (isAdmin || isLdapCheckOk(authentication)) {
            final UserDetails userDetails = retrieveUser(authentication.getName(), null);
            if (userDetails.getUsername() != null) {
                return new UsernamePasswordAuthenticationToken(userDetails, authentication.getCredentials(), userDetails.getAuthorities());
            }
        }
        throw new BadCredentialsException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
    }

    private boolean isLdapCheckOk(Authentication authentication) {
        return ldapAuthenticationService.check(authentication.getName(), (String) authentication.getCredentials());
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
        return currentUserDetailsService.loadUserByUsername(username);
    }

    @Override
    public boolean supports(Class<?> authenticationClass) {
        return Authentication.class.isAssignableFrom(authenticationClass);
    }
}
