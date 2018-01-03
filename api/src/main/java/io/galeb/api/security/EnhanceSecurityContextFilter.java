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

import io.galeb.api.services.AccountDaoService;
import io.galeb.core.entity.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

public class EnhanceSecurityContextFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LogManager.getLogger(EnhanceSecurityContextFilter.class);

    private final AccountDaoService accountDaoService;
    private final LocalAdmin localAdmin;

    EnhanceSecurityContextFilter(AccountDaoService accountDaoService, LocalAdmin localAdmin) {
        this.accountDaoService = accountDaoService;
        this.localAdmin = localAdmin;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filter) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Object remoteUserObj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (remoteUserObj instanceof String) {
                    String remoteUser = (String) remoteUserObj;
                    Account account;
                    if (LocalAdmin.NAME.equals(remoteUser)) {
                        account = localAdmin;
                    } else {
                        account = accountDaoService.find(remoteUser);
                        if (account == null) {
                            account = new Account();
                            account.setUsername(remoteUser);
                            account.setEmail(remoteUser + "@fake." + UUID.randomUUID().toString());
                            account.setAuthorities(AuthorityUtils.createAuthorityList("ROLE_USER"));
                            account = accountDaoService.save(account);
                            LOGGER.warn("Created " + account.getUsername() + " account");
                        }
                    }
                    Authentication auth = new AuthenticationToken(account.getAuthorities(), account);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        filter.doFilter(request, response);
    }

    public static class AuthenticationToken extends AbstractAuthenticationToken {

        private final Account account;

        AuthenticationToken(Collection<? extends GrantedAuthority> authorities, Account account) {
            super(authorities);
            this.account = account;
        }

        @Override
        public Object getCredentials() {
            return account.getApitoken();
        }

        @Override
        public Object getPrincipal() {
            return account;
        }
    }
}
