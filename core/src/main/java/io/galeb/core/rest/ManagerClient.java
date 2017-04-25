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
import io.galeb.core.configuration.SystemEnvs;
import io.galeb.core.entity.*;
import io.galeb.core.services.HttpClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import static io.galeb.core.rest.ManagerSpringRestResponse.*;
import static io.galeb.core.logger.ErrorLogger.logError;

@Component
public class ManagerClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Gson gson = new Gson();

    private final String managerUrl = SystemEnvs.MANAGER_URL.getValue();
    private final String manageruser = SystemEnvs.MANAGER_USER.getValue();
    private final String managerPass = SystemEnvs.MANAGER_PASS.getValue();
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

    public Stream<Target> targetsByEnvName(String environmentName) {
        renewToken();
        if (token != null) {
            long environmentId = getEnvironmentId(environmentName);
            if (environmentId == -1) {
                logger.error("Environment \"" +  environmentName + "\" NOT FOUND");
                return Stream.empty();
            }
            String poolsUrl = managerUrl + "/environment/" + Math.toIntExact(environmentId) + "/pools";
            String body = httpClientService.getResponseBodyWithToken(poolsUrl, token);
            if (!"".equals(body)) {
                PoolList poolList = gson.fromJson(body, PoolList.class);
                return targetsByPoolList(poolList);
            } else {
                logger.error("httpClientService.getResponseBodyWithToken has return body empty");
                resetToken();
                return Stream.empty();
            }
        }
        logger.error("Token is NULL (request problem?)");
        return Stream.empty();
    }

    public Stream<Target> targetsByPoolList(PoolList poolList) {
        try {
            return Arrays.stream(poolList._embedded.pool).parallel().map(pool -> {
                String targetsUrl = pool._links.targets.href + "?size=99999999";
                String bodyTargets = httpClientService.getResponseBodyWithToken(targetsUrl, token);
                if (!"".equals(bodyTargets)) {
                    final TargetList targetList = gson.fromJson(bodyTargets, TargetList.class);
                    return Arrays.stream(targetList._embedded.target).map(target -> {
                        target.setParent(pool);
                        return target;
                    });
                } else {
                    return Stream.empty();
                }
            }).flatMap(s -> s.map(o -> (Target) o));
        } catch (Exception e) {
            logError(e, this.getClass());
            return Stream.empty();
        }
    }

    public long getEnvironmentId(String envName) {
        renewToken();
        if (token != null) {
            String envFindByNameUrl = managerUrl + "/environment/search/findByName?name=" + envName;
            String body = httpClientService.getResponseBodyWithToken(envFindByNameUrl, token);
            EnvironmentFindByName environmentFindByName = gson.fromJson(body, EnvironmentFindByName.class);
            try {
                Environment environment = Arrays.stream(environmentFindByName._embedded.environment).findAny().orElse(null);
                if (environment != null) {
                    return environment.getId();
                }
            } catch (NullPointerException e) {
                logError(e, this.getClass());
                resetToken();
            }
        } else {
            logger.error("Token is NULL (request problem?)");
        }
        return -1;
    }

    public void patch(String targetUrl, String body) {
        if (!httpClientService.patchResponse(targetUrl, body, token)) {
            logger.error("Request FAIL");
            resetToken();
        }
    }

    public void renewToken() {
        if (token == null) {
            String bodyToken = httpClientService.getResponseBodyWithAuth(manageruser, managerPass, tokenUrl);
            if (!"".equals(bodyToken)) {
                Token tokenObj = gson.fromJson(bodyToken, Token.class);
                token = tokenObj.token;
            }
        }
    }

    public VirtualHost virtualhostFindByName(String virtualHostName) {
        return null;
    }

    public Set<Rule> getRules(long id) {
        return Collections.emptySet();
    }

    public Pool poolFindById(Long poolId) {
        return null;
    }

    public BalancePolicy poolGetBalancePolicy(Pool pool) {
        return null;
    }

    public Set<Target> getTargets(Pool pool) {
        return Collections.emptySet();
    }

    public boolean virtualhostsIsEmpty() {
        return false;
    }
}
