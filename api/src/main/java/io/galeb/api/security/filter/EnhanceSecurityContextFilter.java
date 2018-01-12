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

package io.galeb.api.security.filter;

import io.galeb.api.security.LocalAdmin;
import io.galeb.api.services.AccountDaoService;
import io.galeb.api.services.AuditService;
import io.galeb.core.entity.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class EnhanceSecurityContextFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LogManager.getLogger(EnhanceSecurityContextFilter.class);

    private final AccountDaoService accountDaoService;
    private final LocalAdmin localAdmin;
    private final AuditService auditService;

    public EnhanceSecurityContextFilter(AccountDaoService accountDaoService, LocalAdmin localAdmin, AuditService auditService) {
        this.accountDaoService = accountDaoService;
        this.localAdmin = localAdmin;
        this.auditService = auditService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filter) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                final OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
                final OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
                final Object remoteUserObj = authentication.getPrincipal();
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
                            final Map<String, String> newDetails = ((Map<String, Object>) authentication.getUserAuthentication().getDetails())
                                    .entrySet().stream().filter(e -> Objects.nonNull(e.getValue()))
                                    .collect(Collectors.toMap(Map.Entry::getKey, e -> String.format("%s", e.getValue())));
                            account.setDetails(newDetails);
                            String email = newDetails.get("email");
                            account.setEmail(email != null ? email : remoteUser + "@fake." + UUID.randomUUID().toString());
                            account = accountDaoService.save(account);
                            auditService.register("Created " + account.getUsername() + " account (OAuth2 sync)");
                        } else {
                            auditService.register("Using " + account.getUsername() + " account (already created)");
                        }
                    }
                    Authentication auth = new AuthenticationToken(account.getAuthorities(), account, details);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    LOGGER.error("Remote User undefined");
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        filter.doFilter(request, response);
    }

    static class AuthenticationToken extends AbstractAuthenticationToken {

        private final Account account;

        AuthenticationToken(Collection<? extends GrantedAuthority> authorities, Account account, Object details) {
            super(authorities);
            this.account = account;
            setDetails(details);
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
