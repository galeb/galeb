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

import io.galeb.core.entity.Pool;
import io.galeb.core.entity.dto.TargetDTO;
import io.galeb.core.entity.HealthCheck;
import io.galeb.core.entity.HealthStatus;
import io.galeb.core.entity.Target;
import io.galeb.core.enums.SystemEnv;
import io.galeb.core.log.JsonEventToLogger;
import io.galeb.health.util.CallBackQueue;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Optional;

import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

@SuppressWarnings("FieldCanBeLocal")
@Service
public class HealthCheckerService {

    // @formatter:off
    private final boolean keepAlive                   = Boolean.parseBoolean(SystemEnv.TEST_KEEPALIVE.getValue());
    private final int     connectionTimeout           = Integer.parseInt(SystemEnv.TEST_CONN_TIMEOUT.getValue());
    private final int     pooledConnectionIdleTimeout = Integer.parseInt(SystemEnv.TEST_POOLED_IDLE_TIMEOUT.getValue());
    private final int     maxConnectionsPerHost       = Integer.parseInt(SystemEnv.TEST_MAXCONN_PER_HOST.getValue());
    // @formatter:on

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

    public void check(TargetDTO targetDTO) {
        final Target target = targetDTO.getTarget();
        final Pool pool = targetDTO.getPool();
        final String correlation = targetDTO.getCorrelation();
        if (pool != null) {
            try {
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

                RequestBuilder requestBuilder = new RequestBuilder(method.toString()).setHeaders(headers)
                    .setUrl(target.getName() + hcPath).setVirtualHost(hcHost);
                if (method == HealthCheck.HttpMethod.POST ||
                    method == HealthCheck.HttpMethod.PATCH ||
                    method == HealthCheck.HttpMethod.PUT) {

                    requestBuilder.setBody("");
                }
                asyncHttpClient.executeRequest(requestBuilder,
                    new ResponseAsyncCompletionHandler(hcBody, hcStatusCode, target, lastReason,
                        correlation, poolName, hcHost, hcPath, start));
            } catch (Exception e) {
                JsonEventToLogger errorEvent = new JsonEventToLogger(this.getClass());
                errorEvent.put("correlation", correlation);
                errorEvent.sendError(e);
            }
        }
    }

    private String buildHcHostFromTarget(Target target) {
        String hcHost;
        URI targetURI = URI.create(target.getName());
        hcHost = targetURI.getHost() + ":" + targetURI.getPort();
        return hcHost;
    }

    private class ResponseAsyncCompletionHandler extends AsyncCompletionHandler<Response> {

        private final String hcBody;
        private final String hcStatusCode;
        private final Target target;
        private final String lastReason;
        private final String correlation;
        private final String poolName;
        private final String hcHost;
        private final String hcPath;
        private final long start;

        ResponseAsyncCompletionHandler(String hcBody, String hcStatusCode, Target target, String lastReason,
            String correlation, String poolName, String hcHost, String hcPath, long start) {
            this.hcBody = hcBody;
            this.hcStatusCode = hcStatusCode;
            this.target = target;
            this.lastReason = lastReason;
            this.correlation = correlation;
            this.poolName = poolName;
            this.hcHost = hcHost;
            this.hcPath = hcPath;
            this.start = start;
        }

        @Override
        public Response onCompleted(Response response) {
            if (checkFailStatusCode(response) || checkFailBody(response)) return response;
            definePropertiesAndUpdate(HealthStatus.Status.HEALTHY, HealthStatus.Status.HEALTHY.toString());
            return response;
        }

        @Override
        public void onThrowable(Throwable t) {
            definePropertiesAndUpdate(HealthStatus.Status.FAIL, t.getMessage());
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
            sendLog(reason);
            if (!reason.equals(lastReason)) {
                callBackQueue.update(healthStatus, correlation);
            }
            callBackQueue.register(ZONE_ID);
        }

        private void sendLog(String reason) {
            JsonEventToLogger eventToLogger = new JsonEventToLogger(this.getClass());
            eventToLogger.put("pool", poolName);
            eventToLogger.put("expectedBody", hcBody);
            eventToLogger.put("expectedStatusCode", hcStatusCode);
            eventToLogger.put("host", hcHost);
            eventToLogger.put("fullUrl", target.getName() + hcPath);
            eventToLogger.put("connectionTimeout", connectionTimeout);
            eventToLogger.put("result", reason);
            eventToLogger.put("correlation", correlation);
            eventToLogger.put("requestTime", (System.currentTimeMillis() - start));
            eventToLogger.sendInfo();
        }

    }
}
