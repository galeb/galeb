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
import io.galeb.oldapi.services.HttpClientService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

@Service
public class ApiAccountService {

    private static final Logger LOGGER = LogManager.getLogger(ApiAccountService.class);

    @Autowired
    private HttpClientService clientService;

    private AsyncHttpClient httpClient = null;

    private ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    private void init() {
        httpClient = clientService.httpClient();
    }

    @SuppressWarnings("unchecked")
    public Account find(String username) {
        if (httpClient != null) {
            String adminLogin = System.getenv("GALEB_API_ADMIN_NAME");
            String adminPass = System.getenv("GALEB_API_ADMIN_PASS");
            RequestBuilder requestBuilder = new RequestBuilder();
            requestBuilder.setRealm(Dsl.basicAuthRealm(adminLogin, adminPass).setUsePreemptiveAuth(true));
            requestBuilder.setUrl(System.getenv("GALEB_API_URL") + "/account/search/findByUsername?username=" + username + "&projection=apitoken");

            try {
                Response response = httpClient.executeRequest(requestBuilder).get();
                String responseBody = response.getResponseBody();
                if (responseBody != null && !responseBody.isEmpty()) {
                    HashMap json = mapper.readValue(responseBody, HashMap.class);
                    String token = (String) ((HashMap<String, Object>) json).get("apitoken");
                    String self = (String) ((HashMap<String, Object>)((HashMap<String, Object>)((HashMap<String, Object>) json)
                                    .get("_links")).get("self")).get("href");

                    /**
                    LOGGER.warn("TOKEN: " + token + " / SELF: " + self);
                    */

                    Account account = new Account();
                    account.setUsername(username);
                    account.setEmail(username + "@fake");
                    account.setPassword(token);
                    account.setDescription(self);
                    return account;
                }

            } catch (InterruptedException | ExecutionException | IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return null;
    }
}
