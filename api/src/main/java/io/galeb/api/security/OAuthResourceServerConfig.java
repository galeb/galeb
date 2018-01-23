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

import io.galeb.api.security.filter.EnhanceSecurityContextFilter;
import io.galeb.api.services.AccountDaoService;
import io.galeb.api.services.AuditService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("unused")
@Configuration
@EnableResourceServer
public class OAuthResourceServerConfig extends ResourceServerConfigurerAdapter {

    private static final Logger LOGGER = LogManager.getLogger(OAuthResourceServerConfig.class);

    private final LocalAdmin localAdmin;
    private final AccountDaoService accountDaoService;
    private final AuditService auditService;
    private final String login_key;
    private final String reject_key;

    @Autowired
    public OAuthResourceServerConfig(
            LocalAdmin localAdmin,
            AccountDaoService accountDaoService,
            AuditService auditService,
            @Value("${auth.login_key:login}") String login_key,
            @Value("${auth.reject_key}") String reject_key) {
        this.localAdmin = localAdmin;
        this.accountDaoService = accountDaoService;
        this.auditService = auditService;
        this.login_key = login_key;
        this.reject_key = reject_key;
    }

    @Override
    @Transactional
    public void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER).
            and().
                authorizeRequests().regexMatchers(HttpMethod.GET, "/.+/.+/accounts.*").denyAll().
            and().
                authorizeRequests().regexMatchers(HttpMethod.GET, "/.+/.+/teams.*").denyAll().
            and().
                authorizeRequests().regexMatchers(HttpMethod.GET, "/.+/.+/rolegroups.*").denyAll().
            and().
                authorizeRequests().anyRequest().authenticated().
            and().
                httpBasic().
            and()
                .csrf().disable();
        // @formatter:off

        http.addFilterAfter(new EnhanceSecurityContextFilter(accountDaoService, localAdmin, auditService, login_key, reject_key), SecurityContextHolderAwareRequestFilter.class);
    }

}
