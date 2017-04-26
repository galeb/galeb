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
import io.galeb.core.rest.structure.EnvironmentFindByName;
import io.galeb.core.rest.structure.PoolWithLinks;
import io.galeb.core.rest.structure.Pools;
import io.galeb.core.rest.structure.Rules;
import io.galeb.core.rest.structure.Targets;
import io.galeb.core.rest.structure.Token;
import io.galeb.core.rest.structure.VirtualhostFindByName;
import io.galeb.core.rest.structure.Virtualhosts;
import io.galeb.core.services.HttpClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        if (renewToken()) {
            long environmentId = getEnvironmentId(environmentName);
            if (environmentId == -1) {
                logger.error("Environment \"" +  environmentName + "\" NOT FOUND");
                return Stream.empty();
            }
            String poolsUrl = managerUrl + "/environment/" + Math.toIntExact(environmentId) + "/pools";
            String body = httpClientService.getResponseBodyWithToken(poolsUrl, token);
            if (!"".equals(body)) {
                Pools poolList = gson.fromJson(body, Pools.class);
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

    public Stream<Target> targetsByPoolList(Pools poolList) {
        try {
            return Arrays.stream(poolList._embedded.pool).parallel().map(this::getTargetsByPool).flatMap(s -> s.map(o -> (Target) o));
        } catch (Exception e) {
            logError(e, this.getClass());
            return Stream.empty();
        }
    }

    public long getEnvironmentId(String envName) {
        if (renewToken()) {
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

    public VirtualHost getVirtualhostByName(String virtualHostName) {
        if (renewToken()) {
            VirtualhostFindByName virtualhostFindByName = getVirtualhostFindByName(virtualHostName);
            try {
                VirtualHost virtualHost = Arrays.stream(virtualhostFindByName._embedded.virtualhost).findAny().orElse(null);
                if (virtualHost != null) {
                    return virtualHost;
                }
            } catch (NullPointerException e) {
                logError(e, this.getClass());
                resetToken();
            }
        }
        return null;
    }

    private VirtualhostFindByName getVirtualhostFindByName(String virtualHostName) {
        String virtualhostUrl = managerUrl + "/virtualhost/search/findByName?name=" + virtualHostName;
        String body = httpClientService.getResponseBodyWithToken(virtualhostUrl, token);
        return gson.fromJson(body, VirtualhostFindByName.class);
    }

    public Set<Rule> getRulesByVirtualhost(VirtualHost virtualHost) {
        String rulesUrl = managerUrl + "/virtualhost/" + Math.toIntExact(virtualHost.getId()) + "/rules";
        try {
            String body = httpClientService.getResponseBodyWithToken(rulesUrl, token);
            Rules rules = gson.fromJson(body, Rules.class);
            return Arrays.stream(rules._embedded.rule).collect(Collectors.toSet());
        } catch (NullPointerException e) {
            logError(e, this.getClass());
            resetToken();
        }
        return Collections.emptySet();
    }

    public Pool getPoolById(Long poolId) {
        String poolUrl = managerUrl + "/pool/" + Math.toIntExact(poolId);
        try {
            String body = httpClientService.getResponseBodyWithToken(poolUrl, token);
            return gson.fromJson(body, Pool.class);
        } catch (NullPointerException e) {
            logError(e, this.getClass());
            resetToken();
        }
        return null;
    }

    public BalancePolicy getBalancePolicyByPool(Pool pool) {
        try {
            PoolWithLinks poolWithLinks = getPoolWithLinks(pool);
            if (poolWithLinks != null) {
                String balancePolicyLink = poolWithLinks._links.balancePolicy.href;
                String body = httpClientService.getResponseBodyWithToken(balancePolicyLink, token);
                return gson.fromJson(body, BalancePolicy.class);
            }
        } catch (NullPointerException e) {
            logError(e, this.getClass());
            resetToken();
        }
        return null;
    }

    private PoolWithLinks getPoolWithLinks(Pool pool) {
        try {
            String poolUrl = managerUrl + "/pool/" + pool.getId();
            String body = httpClientService.getResponseBodyWithToken(poolUrl, token);
            return gson.fromJson(body, PoolWithLinks.class);
        } catch (NullPointerException e) {
            logError(e, this.getClass());
            resetToken();
        }
        return null;
    }

    public Stream<Target> getTargetsByPool(Pool pool) {
        PoolWithLinks poolWithLinks = getPoolWithLinks(pool);
        try {
            if (poolWithLinks != null) {
                String targetsUrl = poolWithLinks._links.targets.href + "?size=99999999";
                String bodyTargets = httpClientService.getResponseBodyWithToken(targetsUrl, token);
                if (!"".equals(bodyTargets)) {
                    final Targets targetList = gson.fromJson(bodyTargets, Targets.class);
                    return Arrays.stream(targetList._embedded.target).map(target -> {
                        target.setParent(pool);
                        return target;
                    });
                }
            }
        } catch (NullPointerException e) {
            logError(e, this.getClass());
            resetToken();
        }
        return Stream.empty();
    }

    public boolean isVirtualhostsEmpty() {
        String virtualhostsUrl = managerUrl + "/virtualhost";
        try {
            String body = httpClientService.getResponseBodyWithToken(virtualhostsUrl, token);
            Virtualhosts virtualhosts = gson.fromJson(body, Virtualhosts.class);
            return virtualhosts._embedded.virtualhost.length == 0;
        } catch (NullPointerException e) {
            logError(e, this.getClass());
            resetToken();
        }
        return true;
    }
}
