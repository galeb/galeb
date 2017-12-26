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
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.util.Assert;

public class LocalAdmin {

    private static final Logger LOGGER = LogManager.getLogger(LocalAdmin.class);

    private static Account localAdmin = null;

    public static final String NAME = "admin";

    private LocalAdmin() {

    }

    public static Account get(String apiToken) {
        if (localAdmin == null) {
            localAdmin = new Account();
            localAdmin.setUsername(NAME);
            localAdmin.setPassword(apiToken);
            localAdmin.setAuthorities(AuthorityUtils.createAuthorityList("ROLE_USER"));
            LOGGER.info(">>> Local Token: " + apiToken);
        }

        return localAdmin;
    }

    public static Account get() {
        Assert.notNull(localAdmin, "LocalAdmin is NULL");
        return localAdmin;
    }
}
