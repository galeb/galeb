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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
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

    @Autowired
    public OAuthResourceServerConfig(LocalAdmin localAdmin, AccountDaoService accountDaoService) {
        this.localAdmin = localAdmin;
        this.accountDaoService = accountDaoService;
    }

    @Override
    @Transactional
    public void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER).
            and().
                authorizeRequests().anyRequest().authenticated().
            and().
                httpBasic().
            and()
                .csrf().disable();
        // @formatter:off

        http.addFilterAfter(new EnhanceSecurityContextFilter(accountDaoService, localAdmin), SecurityContextHolderAwareRequestFilter.class);
    }

}
