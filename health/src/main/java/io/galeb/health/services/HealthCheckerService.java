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

import io.galeb.core.entity.HealthCheck;
import io.galeb.core.entity.HealthStatus;
import io.galeb.core.entity.Pool;
import io.galeb.core.entity.Target;
import io.galeb.core.enums.SystemEnv;
import io.galeb.health.util.CallBackQueue;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Optional;

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
    private static final String ZONE_ID = SystemEnv.ZONE_ID.getValue();

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

    public void check(Target target) {
        Pool pool = target.getPool();
        if (pool != null) {
            final String poolName = pool.getName();
            final String hcPath = Optional.ofNullable(pool.getHcPath()).orElse("/");
            final String hcStatusCode = Optional.ofNullable(pool.getHcHttpStatusCode()).orElse("");
            final String hcBody = Optional.ofNullable(pool.getHcBody()).orElse("");
            final String hcHost = Optional.ofNullable(pool.getHcHost()).orElse(buildHcHostFromTarget(target));
            final HealthCheck.HttpMethod method = pool.getHcHttpMethod();
            final HttpHeaders headers = new DefaultHttpHeaders();
            pool.getHcHeaders().forEach(headers::add);

            final String lastReason = target.getHealthStatus().stream()
                    .filter(hs -> ZONE_ID.equals(hs.getSource()))
                    .map(HealthStatus::getStatusDetailed).findAny().orElse("");
            long start = System.currentTimeMillis();

            RequestBuilder requestBuilder = new RequestBuilder(method.toString()).setHeaders(headers).setUrl(target.getName() + hcPath).setVirtualHost(hcHost);
            if (method == HealthCheck.HttpMethod.POST || method == HealthCheck.HttpMethod.PATCH || method == HealthCheck.HttpMethod.PUT) {
                requestBuilder.setBody("");
            }
            asyncHttpClient.executeRequest(requestBuilder, new AsyncCompletionHandler<Response>() {
                @Override
                public Response onCompleted(Response response) {
                    if (checkFailStatusCode(response) || checkFailBody(response)) return response;
                    definePropertiesAndUpdate(HealthStatus.Status.HEALTHY, HealthStatus.Status.HEALTHY.toString());
                    return response;
                }

                @Override
                public void onThrowable(Throwable t) {
                    definePropertiesAndUpdate(HealthStatus.Status.UNKNOWN, t.getMessage());
                }

                private boolean checkFailBody(Response response) {
                    if (!"".equals(hcBody)) {
                        String body = response.getResponseBody();
                        if (body != null && !body.isEmpty() && !body.contains(hcBody)) {
                            definePropertiesAndUpdate(HealthStatus.Status.FAIL, "Body check FAIL");
                            return true;
                        }
                    }
                    return false;
                }

                private boolean checkFailStatusCode(Response response) {
                    if (!"".equals(hcStatusCode)) {
                        String statusCodeStr = String.valueOf(response.getStatusCode());
                        if (!hcStatusCode.equals(statusCodeStr)) {
                            definePropertiesAndUpdate(HealthStatus.Status.FAIL, "HTTP Status Code check FAIL");
                            return true;
                        }
                    }
                    return false;
                }

                private void definePropertiesAndUpdate(HealthStatus.Status status, String reason) {
                    HealthStatus healthStatus = new HealthStatus();
                    healthStatus.setTarget(target);
                    healthStatus.setSource(ZONE_ID);
                    healthStatus.setStatus(status);
                    healthStatus.setStatusDetailed(reason);
                    String logMessage = buildLogMessage(reason);
                    if (status.equals(HealthStatus.Status.HEALTHY)) {
                        logger.info(logMessage);
                    } else {
                        logger.warn(logMessage);
                    }
                    if (!reason.equals(lastReason)) {
                        callBackQueue.update(healthStatus);
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
    }

    private String buildHcHostFromTarget(Target target) {
        String hcHost;
        URI targetURI = URI.create(target.getName());
        hcHost = targetURI.getHost() + ":" + targetURI.getPort();
        return hcHost;
    }

}
