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

package io.galeb.router.handlers.completionListeners;

import io.galeb.core.enums.SystemEnv;
import io.galeb.router.client.hostselectors.ClientStatisticsMarker;
import io.galeb.router.client.hostselectors.HostSelector;
import io.galeb.router.services.StatsdClientService;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import static io.galeb.router.handlers.PoolHandler.POOL_NAME;

@Component
public class StatsdCompletionListener extends ProcessorLocalStatusCode implements ExchangeCompletionListener {

    private static final String UNDEF = "UNDEF";

    private static final String VH_PREFIX   = "VH_";
    private static final String ENV_PREFIX  = "ENV_";
    private static final String POOL_PREFIX = "POOL_";

    private final Log logger = LogFactory.getLog(this.getClass());

    private final boolean sendOpenconnCounter = Boolean.parseBoolean(SystemEnv.SEND_OPENCONN_COUNTER.getValue());

    private static final String ENVIRONMENT_NAME = SystemEnv.ENVIRONMENT_NAME.getValue().replaceAll("-","_").toLowerCase();

    private final StatsdClientService statsdClient;

    @Autowired
    public StatsdCompletionListener(StatsdClientService statsdClient) {
        this.statsdClient = statsdClient;
    }

    @Override
    public void exchangeEvent(HttpServerExchange exchange, NextListener nextListener) {
        try {
            String poolName = exchange.getAttachment(POOL_NAME);
            String virtualhost = exchange.getHostName();
            virtualhost = virtualhost != null ? virtualhost : UNDEF;
            String targetUri = exchange.getAttachment(HostSelector.REAL_DEST);
            targetUri = targetUri != null ? targetUri : virtualhost + "__" + extractUpstreamField(exchange.getResponseHeaders(), targetUri);
            final boolean targetIsUndef = UNDEF.equals(targetUri);

            final Integer statusCode = exchange.getStatusCode();
            final String method = exchange.getRequestMethod().toString();
            final Integer responseTime = getResponseTime(exchange);
            final String statsdKeyFull = cleanUpKey(VH_PREFIX + virtualhost) + "." + cleanUpKey(targetUri);
            final String statsdKeyVirtualHost = cleanUpKey(VH_PREFIX + virtualhost);
            final String statsdKeyEnvironmentName = ENV_PREFIX + ENVIRONMENT_NAME;

            Set<String> keys = new HashSet<>();
            keys.add(statsdKeyFull);
            keys.add(statsdKeyVirtualHost);
            keys.add(statsdKeyEnvironmentName);
            if (poolName != null) {
                final String statsdKeyPool = cleanUpKey(POOL_PREFIX + poolName);
                final String statsdKeyPoolTarget = cleanUpKey(POOL_PREFIX + poolName + "." + cleanUpKey(targetUri));
                keys.add(statsdKeyPool);
                keys.add(statsdKeyPoolTarget);
            }

            sendStatusCodeCount(keys, statusCode, targetIsUndef);
            sendHttpMethodCount(keys, method);
            sendResponseTime(keys, responseTime, targetIsUndef);

            if (sendOpenconnCounter) {
                final Integer clientOpenConnection = exchange.getAttachment(ClientStatisticsMarker.TARGET_CONN);
                String statsdKeyEnvironmentNameFull = statsdKeyEnvironmentName + "." + cleanUpKey(targetUri);
                Set<String> keysToConnCount = new HashSet<>();
                keysToConnCount.add(statsdKeyFull);
                keysToConnCount.add(statsdKeyEnvironmentNameFull);
                sendActiveConnCount(keysToConnCount, clientOpenConnection, targetIsUndef);
            }

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } finally {
            nextListener.proceed();
        }
    }

    private void sendStatusCodeCount(Set<String> keys, Integer statusCode, boolean targetIsUndef) {
        int realStatusCode = targetIsUndef ? 503 : statusCode;
        keys.stream().forEach(key -> statsdClient.incr(key + ".httpCode" + realStatusCode));
    }

    private void sendActiveConnCount(Set<String> keys, Integer clientOpenConnection, boolean targetIsUndef) {
        int conn = (clientOpenConnection != null && !targetIsUndef) ? clientOpenConnection : 0;
        keys.stream().forEach(key -> statsdClient.gauge(key + ".activeConns", conn));
    }

    private void sendHttpMethodCount(Set<String> keys, String method) {
        keys.stream().forEach(key -> statsdClient.count(key + ".httpMethod." + method, 1));
    }

    private void sendResponseTime(Set<String> keys, long requestTime, boolean targetIsUndef) {
        long realRequestTime = targetIsUndef ? 0 : requestTime;
        keys.stream().forEach(key -> statsdClient.timing(key + ".requestTime", realRequestTime));
    }

    private int getResponseTime(HttpServerExchange exchange) {
        return Math.round(Float.parseFloat(responseTimeAttribute.readAttribute(exchange)));
    }

    private String cleanUpKey(String str) {
        return str.replaceAll("http://", "").replaceAll("[.:]", "_");
    }
}
