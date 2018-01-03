/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
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

import io.galeb.core.entity.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.google.common.hash.Hashing.sha256;

@Component
public class LocalAdmin extends Account {

    private static final Logger LOGGER = LogManager.getLogger(LocalAdmin.class);

    public static final String NAME = "admin";

    public LocalAdmin(@Value("${auth.localtoken:UNDEF}") String localAdminToken) {
        setUsername(NAME);
        setAuthorities(AuthorityUtils.createAuthorityList("ROLE_USER"));
        if ("UNDEF".equals(localAdminToken)) {
            localAdminToken = sha256().hashBytes(UUID.randomUUID().toString().getBytes()).toString();
            LOGGER.info(">>> Local Token: " + localAdminToken);
        }
        setPassword(localAdminToken);
    }

    public boolean check(String credential) {
        return getPassword().equals(credential);
    }
}
