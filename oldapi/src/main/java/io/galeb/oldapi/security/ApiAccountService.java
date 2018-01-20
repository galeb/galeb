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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.galeb.core.entity.Account;
import io.galeb.oldapi.services.http.HttpClientService;
import io.galeb.oldapi.services.http.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class ApiAccountService {

    private static final Logger LOGGER = LogManager.getLogger(ApiAccountService.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClientService httpClientService;

    @Autowired
    public ApiAccountService(HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
    }

    @SuppressWarnings("unchecked")
    public Account find(String username) {
        String adminLogin = System.getenv("GALEB_API_ADMIN_NAME");
        String adminPass = System.getenv("GALEB_API_ADMIN_PASS");
        String url = System.getenv("GALEB_API_URL") + "/account/search/findByUsername?username=" + username + "&projection=apitoken";

        Account account = null;
        try {
            Response response = httpClientService.getResponse(url, adminLogin, adminPass);
            String responseBody;
            if (response.hasResponseStatus() && response.getStatusCode() <= 299 && (responseBody = response.getResponseBody()) != null && !responseBody.isEmpty()) {
                HashMap json = mapper.readValue(responseBody, HashMap.class);
                String token = (String) ((HashMap<String, Object>) json).get("apitoken");
                String self = (String) ((HashMap<String, Object>)((HashMap<String, Object>)((HashMap<String, Object>) json)
                                .get("_links")).get("self")).get("href");

                /**
                LOGGER.warn("TOKEN: " + token + " / SELF: " + self);
                */

                account = new Account();
                account.setUsername(username);
                account.setEmail(username + "@fake");
                Map<String, String> details = new HashMap<>();
                details.put("self", self);
                details.put("token", token);
                account.setDetails(details);
            }

        } catch (InterruptedException | ExecutionException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return account;
    }
}
