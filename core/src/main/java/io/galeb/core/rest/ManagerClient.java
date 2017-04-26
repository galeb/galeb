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

    public void getEnvironmentId(String envName, final ResultCallBack resultCallBack) {
        if (renewToken()) {
            final HttpClientService.OnCompletedCallBack callback = body -> {
                long result = -1L;
                EnvironmentFindByName environmentFindByName = gson.fromJson(body, EnvironmentFindByName.class);
                try {
                    Environment environment = Arrays.stream(environmentFindByName._embedded.environment).findAny().orElse(null);
                    if (environment != null) {
                        result = environment.getId();
                    }
                } catch (NullPointerException e) {
                    logError(e, this.getClass());
                    resetToken();
                }
                resultCallBack.onResult(result);
            };

            String envFindByNameUrl = managerUrl + "/environment/search/findByName?name=" + envName;
            httpClientService.getResponseBodyWithToken(envFindByNameUrl, token, callback);
        } else {
            logger.error("Token is NULL (request problem?)");
        }
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

    public void getVirtualhostByName(String virtualHostName, final ResultCallBack resultCallBack) {
        if (renewToken()) {
            final ResultCallBack resultCallBackVirtualhostFindByName = result -> {
                VirtualhostFindByName virtualhostFindByName = (VirtualhostFindByName) result;
                try {
                    VirtualHost virtualHost = Arrays.stream(virtualhostFindByName._embedded.virtualhost).findAny().orElse(null);
                    if (virtualHost != null) {
                        resultCallBack.onResult(virtualHost);
                    }
                } catch (NullPointerException e) {
                    logError(e, this.getClass());
                    resetToken();
                    resultCallBack.onResult(null);
                }
            };
            getVirtualhostFindByName(virtualHostName, resultCallBackVirtualhostFindByName);
        }
    }

    private void getVirtualhostFindByName(String virtualHostName, final ResultCallBack resultCallBack) {
        final HttpClientService.OnCompletedCallBack callback = body -> {
            VirtualhostFindByName result = null;
            try {
                result = gson.fromJson(body, VirtualhostFindByName.class);
            } catch (Exception e) {
                logError(e, this.getClass());
                resetToken();
            }
            resultCallBack.onResult(result);
        };
        String virtualhostUrl = managerUrl + "/virtualhost/search/findByName?name=" + virtualHostName;
        httpClientService.getResponseBodyWithToken(virtualhostUrl, token, callback);
    }

    public void getRulesByVirtualhost(final VirtualHost virtualHost, final ResultCallBack resultCallBack) {
        final HttpClientService.OnCompletedCallBack callback = body -> {
            Set<Rule> result = Collections.emptySet();
            try {
                Rules rules = gson.fromJson(body, Rules.class);
                result = Arrays.stream(rules._embedded.rule).collect(Collectors.toSet());
            } catch (Exception e) {
                logError(e, this.getClass());
                resetToken();
            }
            resultCallBack.onResult(result);
        };

        try {
            String rulesUrl = managerUrl + "/virtualhost/" + Math.toIntExact(virtualHost.getId()) + "/rules";
            httpClientService.getResponseBodyWithToken(rulesUrl, token, callback);
        } catch (NullPointerException e) {
            logError(e, this.getClass());
            resetToken();
        }
    }

    public void getPoolById(Long poolId, final ResultCallBack resultCallBack) {
        final HttpClientService.OnCompletedCallBack callBack = body -> {
            Pool result = null;
            try {
                result = gson.fromJson(body, Pool.class);
            } catch (Exception e) {
                logError(e, this.getClass());
                resetToken();
            }
            resultCallBack.onResult(result);
        };

        String poolUrl = managerUrl + "/pool/" + Math.toIntExact(poolId);
        try {
            httpClientService.getResponseBodyWithToken(poolUrl, token, callBack);
        } catch (NullPointerException e) {
            logError(e, this.getClass());
            resetToken();
        }
    }

    public void getBalancePolicyByPool(Pool pool, final ResultCallBack resultCallBack) {
        final HttpClientService.OnCompletedCallBack callback = body -> {
            BalancePolicy result = null;
            try {
                result = gson.fromJson(body, BalancePolicy.class);
            } catch (Exception e) {
                logError(e,this.getClass());
                resetToken();
            }
            resultCallBack.onResult(result);
        };

        final ResultCallBack resultCallBackPoolWithLinks = result -> {
            PoolWithLinks poolWithLinks = (PoolWithLinks) result;
            try {
                if (poolWithLinks != null) {
                    String balancePolicyLink = poolWithLinks._links.balancePolicy.href;
                    httpClientService.getResponseBodyWithToken(balancePolicyLink, token, callback);
                }
            } catch (NullPointerException e) {
                logError(e, this.getClass());
                resetToken();
            }
        };

        getPoolWithLinks(pool, resultCallBackPoolWithLinks);
    }

    private void getPoolWithLinks(final Pool pool, final ResultCallBack resultCallBack) {
        final HttpClientService.OnCompletedCallBack callBack = body -> {
            PoolWithLinks result = null;
            try {
                result = gson.fromJson(body, PoolWithLinks.class);
            } catch (NullPointerException e) {
                logError(e, this.getClass());
                resetToken();
            }
            resultCallBack.onResult(result);
        };
        try {
            String poolUrl = managerUrl + "/pool/" + pool.getId();
            httpClientService.getResponseBodyWithToken(poolUrl, token, callBack);
        } catch (NullPointerException e) {
            logError(e, this.getClass());
            resetToken();
        }
    }

    public void getTargetsByPool(final Pool pool, final ResultCallBack resultCallBack) {
        final HttpClientService.OnCompletedCallBack callBack = body -> {
            if (!"".equals(body)) {
                Stream<Target> result;
                try {
                    final Targets targetList = gson.fromJson(body, Targets.class);
                    result = Arrays.stream(targetList._embedded.target).map(target -> {
                        target.setParent(pool);
                        return target;
                    });
                    resultCallBack.onResult(result);
                } catch (NullPointerException e) {
                    logError(e, this.getClass());
                    resetToken();
                    resultCallBack.onResult(Stream.empty());
                }
            }
        };

        final ResultCallBack resultCallBackPool = result -> {
            PoolWithLinks poolWithLinks = (PoolWithLinks)result;
            try {
                if (poolWithLinks != null) {
                    String targetsUrl = poolWithLinks._links.targets.href + "?size=99999999";
                    httpClientService.getResponseBodyWithToken(targetsUrl, token, callBack);
                }
            } catch (NullPointerException e) {
                logError(e, this.getClass());
                resetToken();
            }
        };

        getPoolWithLinks(pool, resultCallBackPool);
    }

    public void isVirtualhostsEmpty(final ResultCallBack resultCallBack) {
        String virtualhostsUrl = managerUrl + "/virtualhost";
        HttpClientService.OnCompletedCallBack onCompletedCallBack = body -> {
            try {
                Virtualhosts virtualhosts = gson.fromJson(body, Virtualhosts.class);
                Boolean result = virtualhosts._embedded.virtualhost.length == 0;
                resultCallBack.onResult(result);
            } catch (Exception e) {
                logError(e, this.getClass());
                resultCallBack.onResult(true);
            }
        };
        try {
            httpClientService.getResponseBodyWithToken(virtualhostsUrl, token, onCompletedCallBack);
        } catch (NullPointerException e) {
            logError(e, this.getClass());
            resetToken();
        }
    }

    public void getTargets(final ResultCallBack resultCallBack) {
        HttpClientService.OnCompletedCallBack onCompletedCallBack = body -> {
            Target[] result = new Target[0];
            try {
                Targets targets = gson.fromJson(body, Targets.class);
                result = targets._embedded.target;
            } catch (Exception e) {
                logError(e, this.getClass());
            }
            resultCallBack.onResult(result);
        };
        try {
            String targetsUrl = managerUrl + "/target";
            httpClientService.getResponseBodyWithToken(targetsUrl, token, onCompletedCallBack);
        } catch (NullPointerException e) {
            logError(e, this.getClass());
            resetToken();
        }
    }

    public interface ResultCallBack {
        void onResult(Object result);
    }
}
