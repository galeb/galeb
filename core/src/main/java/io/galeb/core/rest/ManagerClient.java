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

package io.galeb.core.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.galeb.core.configuration.SystemEnv;
import io.galeb.core.logger.ErrorLogger;
import io.galeb.core.rest.structure.FullVirtualhosts;
import io.galeb.core.rest.structure.Token;
import io.galeb.core.services.HttpClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.galeb.core.logger.ErrorLogger.logError;

@Component
public class ManagerClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();

    private final String managerUrl = SystemEnv.MANAGER_URL.getValue();
    private final String manageruser = SystemEnv.MANAGER_USER.getValue();
    private final String managerPass = SystemEnv.MANAGER_PASS.getValue();
    private final String tokenUrl = managerUrl + "/token";
    private final HttpClientService httpClientService;
    private String token = null;

    @Autowired
    public ManagerClient(final HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
    }

    public void resetToken() {
        this.token = null;
    }

    public void getVirtualhosts(String envname, ResultCallBack resultCallBack) {
        if (renewToken()) {
            HttpClientService.OnCompletedCallBack callback = body -> {
                if (body != null) {
                    try {
                        FullVirtualhosts virtualhosts = gson.fromJson(body, FullVirtualhosts.class);
                        resultCallBack.onResult(virtualhosts);
                    } catch (Exception e) {
                        ErrorLogger.logError(e, this.getClass());
                        resultCallBack.onResult(null);
                    }
                } else {
                    resultCallBack.onResult(null);
                }
            };
            httpClientService.getResponseBodyWithToken(managerUrl + "/virtualhostscached/" + envname, token, callback);
        } else {
            logger.error("Token is NULL (request problem?)");
        }
    }

    public boolean renewToken() {
        if (token == null) {
            String bodyToken = httpClientService.getResponseBodyWithAuth(manageruser, managerPass, tokenUrl);
            if (!"".equals(bodyToken)) {
                Token tokenObj = gson.fromJson(bodyToken, Token.class);
                if (tokenObj != null) {
                    token = tokenObj.token;
                } else {
                    logError(new RuntimeException("renewToken problem"), this.getClass());
                    return false;
                }
            }
        }
        return true;
    }

    public interface ResultCallBack {
        void onResult(Object result);
    }
}
