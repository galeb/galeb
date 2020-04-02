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
import io.galeb.core.entity.HealthStatus.Status;
import io.galeb.core.entity.Target;
import io.galeb.core.entity.dto.TargetDTO;
import io.galeb.core.enums.SystemEnv;
import io.galeb.core.log.JsonEventToLogger;
import io.galeb.health.util.CallBackQueue;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.asynchttpclient.cookie.CookieStore;
import org.asynchttpclient.uri.Uri;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

@SuppressWarnings("FieldCanBeLocal")
@Service
public class HealthCheckerService {

    private static final Class ROOT_CLASS = HealthCheckerService.class;

    private static final CookieStore NULL_COOKIE_STORE = new CookieStore() {
        @Override
        public void add(Uri uri, Cookie cookie) { }

        @Override
        public List<Cookie> get(Uri uri) {
            return Collections.emptyList();
        }

        @Override
        public List<Cookie> getAll() {
            return Collections.emptyList();
        }

        @Override
        public boolean remove(Predicate<Cookie> predicate) {
            return true;
        }

        @Override
        public boolean clear() {
            return true;
        }
    };

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
                .setCookieStore(NULL_COOKIE_STORE)
                .setKeepAlive(keepAlive)
                .setConnectTimeout(connectionTimeout)
                .setPooledConnectionIdleTimeout(pooledConnectionIdleTimeout)
                .setMaxConnectionsPerHost(maxConnectionsPerHost)
                .setUserAgent(HEALTHCHECKER_USERAGENT).build());
    }

    public void check(TargetDTO targetDTO) {
        try {
            final Boolean hcTcpOnly = (Boolean) targetDTO.getProperties().get(TargetDTO.HC_TCP_ONLY);
            
            if (hcTcpOnly != null && hcTcpOnly) {
                checkTcp(targetDTO);
            } else {
                checkHttp(targetDTO);
            }
        } catch (Exception e) {
            JsonEventToLogger errorEvent = new JsonEventToLogger(ROOT_CLASS);
            errorEvent.put("correlation", targetDTO.getCorrelation());
            errorEvent.put("short_message", "Error processing check");
            errorEvent.sendError(e);
        }
    }

    private void checkTcp(TargetDTO targetDTO) {

        long startTime = System.currentTimeMillis();

        String reason;
        Status status;
        Socket socket = new Socket();
        try {
            URL url = new URL(targetDTO.getTarget().getName());
            String address = url.getHost();

            int port = url.getPort();
            if (port == -1) {
                port = url.getProtocol().equals("http") ? 80 : 443;
            }

            socket.setSoTimeout(connectionTimeout);
            socket.connect(new InetSocketAddress(address, port), connectionTimeout);
            status = Status.HEALTHY;
            reason = status.toString();
        } catch (MalformedURLException e) {
            status = Status.FAIL;
            reason = "Malformed url: " + targetDTO.getTarget().getName();
        } catch (SocketTimeoutException e) {
            status = Status.FAIL;
            reason = "Fail tcp connection timeout";
        } catch (IOException e) {
            status = Status.FAIL;
            reason = "Fail to estabilished tcp connection";
        } finally {
            if (socket != null) {                
                try {
                    socket.close();
                } catch (Exception e) {}
            }
        }

        definePropertiesAndUpdate(targetDTO, reason, status, startTime);
    }

    @SuppressWarnings("unchecked")
    private void checkHttp(TargetDTO targetDTO) {
        final Map<String, Object> properties = targetDTO.getProperties();
        final String correlation = targetDTO.getCorrelation();
        final Target target = targetDTO.getTarget();
        final String poolName = (String) properties.get(TargetDTO.POOL_NAME);
        final String hcPath = (String) properties.get(TargetDTO.HC_PATH);
        final String hcStatusCode = (String) properties.get(TargetDTO.HC_HTTP_STATUS_CODE);
        final String hcBody = (String) properties.get(TargetDTO.HC_BODY);
        final String hcHost = (String) properties.get(TargetDTO.HC_HOST);
        final HealthCheck.HttpMethod method = HealthCheck.HttpMethod.valueOf((String) properties.get(TargetDTO.HC_HTTP_METHOD));
        final HttpHeaders headers = new DefaultHttpHeaders();
        ((Map<String, String>) properties.get(TargetDTO.HC_HEADERS)).forEach(headers::add);
        final String lastReason = Optional.ofNullable(targetDTO.getHealthStatus(ZONE_ID)
            .orElse(new HealthStatus()).getStatusDetailed()).orElse("");
        long start = System.currentTimeMillis();

        RequestBuilder requestBuilder = new RequestBuilder(method.toString()).setHeaders(headers)
            .setUrl(target.getName() + hcPath).setVirtualHost(hcHost);

        if (method == HealthCheck.HttpMethod.POST ||
            method == HealthCheck.HttpMethod.PATCH ||
            method == HealthCheck.HttpMethod.PUT) {

            requestBuilder.setBody(hcBody);
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

            private void definePropertiesAndUpdate(Status status, String reason) {
                HealthStatus healthStatus = new HealthStatus();
                healthStatus.setSource(ZONE_ID);
                healthStatus.setStatus(status);
                healthStatus.setStatusDetailed(reason);
                target.setHealthStatus(Collections.singleton(healthStatus));
                sendLog(reason);
                if (!reason.equals(lastReason)) {
                    callBackQueue.update(targetDTO);
                }
            }

            private void sendLog(String reason) {
                JsonEventToLogger eventToLogger = new JsonEventToLogger(ROOT_CLASS);
                eventToLogger.put("pool", poolName);
                eventToLogger.put("short_message", "Processing check");
                eventToLogger.put("expectedBody", hcBody);
                eventToLogger.put("expectedStatusCode", hcStatusCode);
                eventToLogger.put("hc_host", hcHost);
                eventToLogger.put("fullUrl", target.getName() + hcPath);
                eventToLogger.put("connectionTimeout", connectionTimeout);
                eventToLogger.put("result", reason);
                eventToLogger.put("correlation", correlation);
                eventToLogger.put("requestTime", (System.currentTimeMillis() - start));
                eventToLogger.sendInfo();
            }
        });
    }
    
    private void definePropertiesAndUpdate(TargetDTO targetDTO, String reason, Status status, long startTime) {
        Target target = targetDTO.getTarget();
        final String lastReason = Optional.ofNullable(targetDTO.getHealthStatus(ZONE_ID)
                .orElse(new HealthStatus()).getStatusDetailed()).orElse("");
        HealthStatus healthStatus = new HealthStatus();
        healthStatus.setSource(ZONE_ID);
        healthStatus.setStatus(status);
        healthStatus.setStatusDetailed(reason);
        target.setHealthStatus(Collections.singleton(healthStatus));
        sendLog(targetDTO, reason, startTime);

        if (!reason.equals(lastReason)) {
            callBackQueue.update(targetDTO);
        }
    }
    
    private void sendLog(TargetDTO targetDTO, String reason, long startTime) {
        Map<String, Object> properties = targetDTO.getProperties();
        JsonEventToLogger eventToLogger = new JsonEventToLogger(ROOT_CLASS);
        eventToLogger.put("pool", (String) properties.get(TargetDTO.POOL_NAME));
        eventToLogger.put("short_message", "Processing check");
        eventToLogger.put("fullUrl", targetDTO.getTarget().getName());
        eventToLogger.put("connectionTimeout", connectionTimeout);
        eventToLogger.put("result", reason);
        eventToLogger.put("correlation", targetDTO.getCorrelation());
        eventToLogger.put("requestTime", (System.currentTimeMillis() - startTime));
        eventToLogger.sendInfo();
    }
}
