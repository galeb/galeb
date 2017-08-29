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

package io.galeb.health.services;

import com.google.gson.Gson;
import io.galeb.core.enums.SystemEnv;
import io.galeb.core.entity.Target;
import io.galeb.core.enums.EnumHealthState;
import io.galeb.health.util.CallBackQueue;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static io.galeb.core.enums.EnumHealthState.*;
import static io.galeb.core.enums.EnumPropHealth.*;
import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

@SuppressWarnings("FieldCanBeLocal")
@Service
public class HealthCheckerService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final boolean keepAlive                   = Boolean.parseBoolean(SystemEnv.TEST_KEEPALIVE.getValue());
    private final int     connectionTimeout           = Integer.parseInt(SystemEnv.TEST_CONN_TIMEOUT.getValue());
    private final int     pooledConnectionIdleTimeout = Integer.parseInt(SystemEnv.TEST_POOLED_IDLE_TIMEOUT.getValue());
    private final int     maxConnectionsPerHost       = Integer.parseInt(SystemEnv.TEST_MAXCONN_PER_HOST.getValue());

    private final CallBackQueue callBackQueue;
    private final AsyncHttpClient asyncHttpClient;

    private static final String HEALTHCHECKER_USERAGENT = "Galeb_HealthChecker/1.0";

    @Autowired
    public HealthCheckerService(final CallBackQueue callBackQueue) {
        this.callBackQueue = callBackQueue;
        this.asyncHttpClient = asyncHttpClient(config()
                .setFollowRedirect(false)
                .setSoReuseAddress(true)
                .setKeepAlive(keepAlive)
                .setConnectTimeout(connectionTimeout)
                .setPooledConnectionIdleTimeout(pooledConnectionIdleTimeout)
                .setMaxConnectionsPerHost(maxConnectionsPerHost)
                .setUserAgent(HEALTHCHECKER_USERAGENT).build());
    }

    @SuppressWarnings({"FutureReturnValueIgnored", "unused"})
    @JmsListener(destination = "galeb-health", concurrency = "5-5")
    public void check(String targetStr) throws ExecutionException, InterruptedException {
        final Target target = new Gson().fromJson(targetStr, Target.class);
        final String poolName = target.getParent().getName();
        final Map<String, String> properties = target.getParent().getProperties();
        final String hcPath = Optional.ofNullable(properties.get(PROP_HEALTHCHECK_PATH.toString())).orElse("/");
        final String hcStatusCode = Optional.ofNullable(properties.get(PROP_HEALTHCHECK_CODE.toString())).orElse("");
        final String hcBody = Optional.ofNullable(properties.get(PROP_HEALTHCHECK_RETURN.toString())).orElse("");
        final String hcHost = Optional.ofNullable(properties.get(PROP_HEALTHCHECK_HOST.toString())).orElse(buildHcHostFromTarget(target));
        final String lastReason = target.getProperties().get(PROP_STATUS_DETAILED.toString());
        long start = System.currentTimeMillis();

        RequestBuilder requestBuilder = new RequestBuilder("GET").setUrl(target.getName() + hcPath).setVirtualHost(hcHost);
        asyncHttpClient.executeRequest(requestBuilder, new AsyncCompletionHandler<Response>() {
            @Override
            public Response onCompleted(Response response) throws Exception {
                if (checkFailStatusCode(response) || checkFailBody(response)) return response;
                definePropertiesAndUpdate(OK, OK.toString());
                return response;
            }

            @Override
            public void onThrowable(Throwable t) {
                definePropertiesAndUpdate(UNKNOWN, t.getMessage());
            }

            private boolean checkFailBody(Response response) {
                if (!"".equals(hcBody)) {
                    String body = response.getResponseBody();
                    if (body != null && !body.isEmpty() && !body.contains(hcBody)) {
                        definePropertiesAndUpdate(FAIL, "Body check FAIL");
                        return true;
                    }
                }
                return false;
            }

            private boolean checkFailStatusCode(Response response) {
                if (!"".equals(hcStatusCode)) {
                    String statusCodeStr = String.valueOf(response.getStatusCode());
                    if (!hcStatusCode.equals(statusCodeStr)) {
                        definePropertiesAndUpdate(FAIL, "HTTP Status Code check FAIL");
                        return true;
                    }
                }
                return false;
            }

            private void definePropertiesAndUpdate(EnumHealthState state, String reason) {
                String newHealthyState = state.toString();

                target.getProperties().put(PROP_HEALTHY.toString(), newHealthyState);
                target.getProperties().put(PROP_STATUS_DETAILED.toString(), reason);
                String logMessage = buildLogMessage(reason);
                if (state.equals(OK)) {
                    logger.info(logMessage);
                } else {
                    logger.warn(logMessage);
                }
                if (lastReason == null || !reason.equals(lastReason)) {
                    callBackQueue.update(target);
                }
            }

            private String buildLogMessage(String reason) {
                return "Pool " + poolName + " -> " + "Test Params: { "
                            + "ExpectedBody:\"" + hcBody + "\", "
                            + "ExpectedStatusCode:" + hcStatusCode + ", "
                            + "Host:\"" + hcHost + "\", "
                            + "FullUrl:\"" + target.getName() + hcPath + "\", "
                            + "ConnectionTimeout:" + connectionTimeout + "ms }, "
                        + "Result: [ " + reason
                            + " (request time: " + (System.currentTimeMillis() - start) + " ms) ]";
            }

        });
    }

    private String buildHcHostFromTarget(Target target) {
        String hcHost;URI targetURI = URI.create(target.getName());
        hcHost = targetURI.getHost() + ":" + targetURI.getPort();
        return hcHost;
    }

}
