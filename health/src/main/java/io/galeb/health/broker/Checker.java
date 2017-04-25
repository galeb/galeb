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

package io.galeb.health.broker;

import io.galeb.core.configuration.SystemEnvs;
import io.galeb.core.entity.Target;
import io.galeb.health.util.TargetStamper;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static io.galeb.health.util.TargetStamper.HcState.*;
import static io.galeb.health.util.TargetStamper.*;
import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

@Component
public class Checker {

    public static final AtomicLong LAST_CALL = new AtomicLong(0L);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String connectionTimeout = SystemEnvs.TEST_CONN_TIMEOUT.getValue();

    private final TargetStamper targetStamper;

    private final AsyncHttpClient asyncHttpClient;

    @Autowired
    public Checker(final TargetStamper targetStamper) {
        this.targetStamper = targetStamper;
        this.asyncHttpClient = asyncHttpClient(config()
                .setFollowRedirect(false)
                .setSoReuseAddress(true)
                .setKeepAlive(false)
                .setConnectTimeout(Integer.parseInt(connectionTimeout))
                .setPooledConnectionIdleTimeout(1)
                .setMaxConnectionsPerHost(1).build());
    }

    @SuppressWarnings("unused")
    @JmsListener(destination = "galeb-health", concurrency = "5-5", containerFactory = "containerFactory")
    public void check(Target target) throws ExecutionException, InterruptedException {
        LAST_CALL.set(System.currentTimeMillis());
        final Map<String, String> properties = target.getProperties();
        final AtomicReference<String> hcPath = new AtomicReference<>(properties.get(PROP_HEALTHCHECK_PATH));
        hcPath.compareAndSet(null,"/");
        final String hcStatusCode = properties.get(PROP_HEALTHCHECK_CODE);
        final String hcBody = properties.get(PROP_HEALTHCHECK_RETURN);
        String hcHost = properties.get(PROP_HEALTHCHECK_HOST);
        if (hcHost == null) {
            URI targetURI = URI.create(target.getName());
            hcHost = targetURI.getHost() + ":" + targetURI.getPort();
        }
        final String realHost = hcHost;
        final String lastReason = properties.get(PROP_STATUS_DETAILED);
        long start = System.currentTimeMillis();

        RequestBuilder requestBuilder = new RequestBuilder("GET").setUrl(target.getName() + hcPath.get()).setVirtualHost(realHost);
        asyncHttpClient.executeRequest(requestBuilder, new AsyncCompletionHandler<Response>() {
            @Override
            public Response onCompleted(Response response) throws Exception {
                if (checkFailStatusCode(response)) return response;
                if (checkFailBody(response)) return response;
                definePropertiesAndUpdate(OK, OK.toString());
                return response;
            }

            @Override
            public void onThrowable(Throwable t) {
                definePropertiesAndUpdate(UNKNOWN, t.getMessage());
            }

            private boolean checkFailBody(Response response) {
                if (hcBody != null) {
                    String body = response.getResponseBody();
                    if (body != null && !body.isEmpty() && !body.contains(hcBody)) {
                        definePropertiesAndUpdate(FAIL, "Body check FAIL");
                        return true;
                    }
                }
                return false;
            }

            private boolean checkFailStatusCode(Response response) {
                if (hcStatusCode != null) {
                    int statusCode = response.getStatusCode();
                    if (statusCode != Integer.parseInt(hcStatusCode)) {
                        definePropertiesAndUpdate(FAIL, "HTTP Status Code check FAIL");
                        return true;
                    }
                }
                return false;
            }

            private void definePropertiesAndUpdate(TargetStamper.HcState state, String reason) {
                String newHealthyState = state.toString();

                target.getProperties().put(PROP_HEALTHY, newHealthyState);
                target.getProperties().put(PROP_STATUS_DETAILED, reason);
                String scheduleId = target.getProperties().get("SCHEDULER_ID");
                String logMessage = "[schedId: " + scheduleId + "] "
                        + "Test Params: { "
                            + "ExpectedBody:\"" + hcBody + "\", "
                            + "ExpectedStatusCode:" + hcStatusCode + ", "
                            + "Host:\"" + realHost + "\", "
                            + "FullUrl:\"" + target.getName() + hcPath.get() + "\", "
                            + "ConnectionTimeout:" + connectionTimeout + "ms }, "
                        + "Result: [ " + reason
                            + " (request time: " + (System.currentTimeMillis() - start) + " ms) ]";
                if (state.equals(OK)) {
                    logger.info(logMessage);
                } else {
                    logger.warn(logMessage);
                }
                if (lastReason == null || !reason.equals(lastReason)) {
                    targetStamper.patchTarget(target);
                }
            }

        });
    }

}
