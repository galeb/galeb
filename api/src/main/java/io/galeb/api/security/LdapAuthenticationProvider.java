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

package io.galeb.api.security;

import io.galeb.api.repository.AccountRepository;
import io.galeb.api.services.LdapAuthenticationService;
import io.galeb.core.entity.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class LdapAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private static final Logger LOGGER = LogManager.getLogger(LdapAuthenticationProvider.class);

    @Autowired
    private CurrentUserDetailsService currentUserDetailsService;

    @Autowired
    private LdapAuthenticationService ldapAuthenticationService;

    @Autowired
    private AccountRepository accountRepository;

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

        UserDetails userDetails;
        if (isLdapCheckOk(authentication)) {
            try {
                userDetails = retrieveUser(authentication.getName(), null);
                return new UsernamePasswordAuthenticationToken(userDetails, authentication.getCredentials(), userDetails.getAuthorities());
            } catch (UsernameNotFoundException e) {
                try {
                    Account account = new Account();
                    account.setUsername(authentication.getName());
                    account.setEmail(authentication.getName());
                    accountRepository.saveByPass(account);
                } catch (Exception e1) {
                    LOGGER.error(e1);
                    throw e;
                }
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
