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

import static io.galeb.router.handlers.PoolHandler.POOL_NAME;

@Component
public class StatsdCompletionListener extends ProcessorLocalStatusCode implements ExchangeCompletionListener {

    private static final String UNDEF      = "UNDEF";
    private static final String ENV_TAG    = SystemEnv.STATSD_ENVIRONMENT_TAG.getValue();
    private static final String VH_TAG     = SystemEnv.STATSD_VIRTUALHOST_TAG.getValue();
    private static final String POOL_TAG   = SystemEnv.STATSD_POOL_TAG.getValue();
    private static final String TARGET_TAG = SystemEnv.STATSD_TARGET_TAG.getValue();

    private static final String ENVIRONMENT_NAME = cleanUpKey(SystemEnv.ENVIRONMENT_NAME.getValue().replaceAll("-","_").toLowerCase());

    private final Log logger = LogFactory.getLog(this.getClass());

    private final boolean sendOpenconnCounter = Boolean.parseBoolean(SystemEnv.SEND_OPENCONN_COUNTER.getValue());

    private final StatsdClientService statsdClient;

    @Autowired
    public StatsdCompletionListener(StatsdClientService statsdClient) {
        this.statsdClient = statsdClient;
    }

    @Override
    public void exchangeEvent(HttpServerExchange exchange, NextListener nextListener) {
        try {
            String virtualhost = exchange.getHostName();
            virtualhost = virtualhost != null ? virtualhost : UNDEF;
            String poolName = exchange.getAttachment(POOL_NAME);
            poolName = poolName != null ? poolName : UNDEF;
            String targetUri = exchange.getAttachment(HostSelector.REAL_DEST);
            targetUri = targetUri != null ? targetUri : extractXGalebErrorHeader(exchange.getResponseHeaders());
            final boolean isTargetUnknown = targetIsUnknown(targetUri);
            final Integer statusCode = exchange.getStatusCode();
            final String method = exchange.getRequestMethod().toString();
            final Integer responseTime = getResponseTime(exchange);

            // @formatter:off
            final String key = ENV_TAG    + "." + ENVIRONMENT_NAME + "." +
                               VH_TAG     + "." + cleanUpKey(virtualhost) + "." +
                               POOL_TAG   + "." + cleanUpKey(poolName) + "." +
                               TARGET_TAG + "." + cleanUpKey(targetUri);
            // @formatter:on

            sendStatusCodeCount(key, statusCode, isTargetUnknown);
            sendHttpMethodCount(key, method);
            sendResponseTime(key, responseTime, isTargetUnknown);
            if (sendOpenconnCounter) sendActiveConnCount(key, exchange.getAttachment(ClientStatisticsMarker.TARGET_CONN), isTargetUnknown);

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } finally {
            nextListener.proceed();
        }
    }

    private void sendStatusCodeCount(String key, Integer statusCode, boolean targetIsUndef) {
        int realStatusCode = targetIsUndef ? 503 : statusCode;
        statsdClient.incr(key + ".httpCode." + realStatusCode);
    }

    private void sendActiveConnCount(String key, Integer clientOpenConnection, boolean targetIsUndef) {
        int conn = (clientOpenConnection != null && !targetIsUndef) ? clientOpenConnection : 0;
        statsdClient.gauge(key + ".activeConns", conn);
    }

    private void sendHttpMethodCount(String key, String method) {
        statsdClient.count(key + ".httpMethod." + method, 1);
    }

    private void sendResponseTime(String key, long requestTime, boolean targetIsUndef) {
        long realRequestTime = targetIsUndef ? 0 : requestTime;
        statsdClient.timing(key + ".requestTime", realRequestTime);
    }

    private int getResponseTime(HttpServerExchange exchange) {
        return Math.round(Float.parseFloat(responseTimeAttribute.readAttribute(exchange)));
    }

    private static String cleanUpKey(String str) {
        return str.replaceAll("http://", "").replaceAll("[.: ]", "_");
    }
}
