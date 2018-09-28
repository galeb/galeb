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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final InMemoryAuthenticatorProvider inMemoryAuthenticatorProvider;
    private final ApiTokenAuthenticationProvider apiTokenAuthenticationProvider;
    private final LdapAuthenticationProvider ldapAuthenticationProvider;

    @Autowired
    public SecurityConfiguration(ApiTokenAuthenticationProvider apiTokenAuthenticationProvider, LdapAuthenticationProvider ldapAuthenticationProvider, InMemoryAuthenticatorProvider inMemoryAuthenticatorProvider) {
        this.apiTokenAuthenticationProvider = apiTokenAuthenticationProvider;
        this.ldapAuthenticationProvider = ldapAuthenticationProvider;
        this.inMemoryAuthenticatorProvider = inMemoryAuthenticatorProvider;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
                .authorizeRequests().regexMatchers("^/swagger.*", "^/webjars/.*", "^/v2/api.*", "^/info", "^/healthcheck.html").permitAll().
            and().
                sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER).
            and().
                authorizeRequests().regexMatchers(HttpMethod.GET, "/.+/.+/(rolegroups).*").denyAll().
            and().
                authorizeRequests().anyRequest().authenticated().
            and().
                httpBasic().
            and()
                .csrf().disable();

        // @formatter:off
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(apiTokenAuthenticationProvider);
        auth.authenticationProvider(ldapAuthenticationProvider);
    }

}
