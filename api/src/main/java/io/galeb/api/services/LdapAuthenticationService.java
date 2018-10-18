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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Service;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Service
public class LdapAuthenticationService {

    private final LdapTemplate ldapTemplate;
    private final LdapContextSource ldapContextSource;

    @Value("${ldap.attrdn}") String attrdn;

    @Autowired
    public LdapAuthenticationService(LdapTemplate ldapTemplate, LdapContextSource ldapContextSource) {
        this.ldapTemplate = ldapTemplate;
        this.ldapContextSource = ldapContextSource;
    }

    public boolean check(String username, String password) {
        final String[] ldapContextSourceUrls = ldapContextSource.getUrls();
        if (ldapContextSourceUrls.length > 0 && "ldap://127.0.0.1:3890?alwaystrue".equals(ldapContextSourceUrls[0])) {
            return true;
        }
        try {
            ldapTemplate.authenticate(query().where(attrdn).is(username), password);
            return true;
        } catch (Exception ignored) {}
        return false;
    }
}
