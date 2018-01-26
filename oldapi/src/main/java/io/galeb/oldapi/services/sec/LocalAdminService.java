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

package io.galeb.oldapi.services.sec;

import io.galeb.core.entity.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.UUID;

import static com.google.common.hash.Hashing.sha256;

@Service
public class LocalAdminService extends Account {

    private static final Logger LOGGER = LogManager.getLogger(LocalAdminService.class);

    public static final String NAME = "admin";

    public LocalAdminService(@Value("${auth.localtoken:UNDEF}") String localAdminToken) {
        setUsername(NAME);
        if ("UNDEF".equals(localAdminToken)) {
            localAdminToken = sha256().hashString(UUID.randomUUID().toString(), Charset.defaultCharset()).toString();
            LOGGER.info(">>> Local Token: " + localAdminToken);
        }
        setPassword(localAdminToken);
    }

    public boolean check(String credential) {
        return getPassword().equals(credential);
    }
}
