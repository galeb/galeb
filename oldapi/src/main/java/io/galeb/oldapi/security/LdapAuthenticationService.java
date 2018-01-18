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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Service
public class LdapAuthenticationService {

    @Autowired
    private LdapTemplate ldapTemplate;

    public boolean check(String username, String password) {
        try {
            ldapTemplate.authenticate(query().where(System.getenv("GALEB_LDAP_DN_ATTRIB"))
                                             .is(username), password);
            return true;
        } catch (Exception ignored) {}
        return false;
    }
}
