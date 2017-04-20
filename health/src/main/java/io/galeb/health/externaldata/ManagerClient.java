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

package io.galeb.health.externaldata;

import com.google.gson.Gson;
import io.galeb.health.SystemEnvs;
import io.galeb.health.services.HttpClientService;
import io.galeb.manager.entity.Pool;
import io.galeb.manager.entity.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static io.galeb.health.broker.Checker.State.UNKNOWN;

@Component
public class ManagerClient {

    public static final String PROP_HEALTHCHECK_RETURN = "hcBody";
    public static final String PROP_HEALTHCHECK_PATH   = "hcPath";
    public static final String PROP_HEALTHCHECK_HOST   = "hcHost";
    public static final String PROP_HEALTHCHECK_CODE   = "hcStatusCode";
    public static final String PROP_HEALTHY            = "healthy";
    public static final String PROP_STATUS_DETAILED    = "status_detailed";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Gson gson = new Gson();
    private final int environmentId = Integer.parseInt(SystemEnvs.ENVIRONMENT_ID.getValue());
    private final String managerUrl = SystemEnvs.MANAGER_URL.getValue();
    private final String manageruser = SystemEnvs.MANAGER_USER.getValue();
    private final String managerPass = SystemEnvs.MANAGER_PASS.getValue();
    private final String poolsUrl = managerUrl + "/environment/" + environmentId + "/pools";
    private final String tokenUrl = managerUrl + "/token";
    private final HttpClientService httpClientService;
    private String token = null;

    static class TargetList {
        TargetEmbedded _embedded;
    }

    static class PoolList {
        PoolEmbedded _embedded;
    }

    private static class TargetEmbedded {
        Target[] target;
    }

    private static class PoolEmbedded {
        PoolExtended[] pool;
    }

    public static class Href {
        String href;
    }

    @SuppressWarnings("unused")
    public static class PoolLinks {
        Href self;
        Href rules;
        Href environment;
        Href project;
        Href balancePolicy;
        Href targets;
    }

    public static class PoolExtended extends Pool {
        PoolLinks _links;
    }

    @SuppressWarnings("unused")
    private static class Token {
        Boolean admin;
        Boolean hasTeam;
        String account;
        String email;
        String token;
    }

    @Autowired
    public ManagerClient(final HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
    }

    public void update(Target target) throws ExecutionException, InterruptedException {
        String targetUrl = managerUrl + "/target/" + target.getId();
        String healthy = target.getProperties().get(PROP_HEALTHY);
        String statusDetailed = target.getProperties().get(PROP_STATUS_DETAILED);
        healthy = healthy != null ? healthy : UNKNOWN.toString();
        statusDetailed = statusDetailed != null ? statusDetailed : UNKNOWN.toString();
        getToken();
        String body = "{\"properties\": { \"" + PROP_HEALTHY + "\":\"" + healthy + "\",\"" + PROP_STATUS_DETAILED + "\":\"" + statusDetailed + "\" }}";
        httpClientService.patchResponse(targetUrl, body, token);
    }

    public Stream<?> targets() throws ExecutionException, InterruptedException {
        getToken();

        String body = httpClientService.getResponseBodyWithToken(poolsUrl, token);
        PoolList poolList = gson.fromJson(body, PoolList.class);

        return copyPoolPropsToTargetsAndGetAll(poolList);
    }

    private Stream<?> copyPoolPropsToTargetsAndGetAll(PoolList poolList) {
        return Arrays.stream(poolList._embedded.pool).parallel().map(pool -> {
            String hcPath = pool.getProperties().get(PROP_HEALTHCHECK_PATH);
            String hcStatusCode = pool.getProperties().get(PROP_HEALTHCHECK_CODE);
            String hcBody = pool.getProperties().get(PROP_HEALTHCHECK_RETURN);
            String hcHost = pool.getProperties().get(PROP_HEALTHCHECK_HOST);

            String targetsUrl = pool._links.targets.href;
            String bodyTargets;
            try {
                bodyTargets = httpClientService.getResponseBodyWithToken(targetsUrl, token);
            } catch (InterruptedException | ExecutionException e) {
                bodyTargets = "";
            }
            if (!"".equals(bodyTargets)) {
                final TargetList targetList = gson.fromJson(bodyTargets, TargetList.class);
                return Arrays.stream(targetList._embedded.target).map(target -> {
                    Map<String, String> properties = getNewProperties(hcPath, hcStatusCode, hcBody, hcHost);
                    target.getProperties().putAll(properties);
                    return target;
                });
            } else {
                return Stream.empty();
            }
        });
    }

    private Map<String, String> getNewProperties(String hcPath, String hcStatusCode, String hcBody, String hcHost) {
        Map<String, String> properties = new HashMap<>();
        properties.put(PROP_HEALTHCHECK_PATH, hcPath);
        properties.put(PROP_HEALTHCHECK_HOST, hcHost);
        properties.put(PROP_HEALTHCHECK_CODE, hcStatusCode);
        properties.put(PROP_HEALTHCHECK_RETURN, hcBody);
        return properties;
    }

    private void getToken() throws InterruptedException, ExecutionException {
        if (token == null) {
            String bodyToken = httpClientService.getResponseBodyWithAuth(manageruser, managerPass, tokenUrl);
            Token tokenObj = gson.fromJson(bodyToken, Token.class);
            token = tokenObj.token;
        }
    }

}
