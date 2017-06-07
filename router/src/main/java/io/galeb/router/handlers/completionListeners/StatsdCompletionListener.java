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

@Component
public class StatsdCompletionListener extends ProcessorLocalStatusCode implements ExchangeCompletionListener {

    private static final String UNDEF = "UNDEF";

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
            String targetUri = exchange.getAttachment(HostSelector.REAL_DEST);
            targetUri = targetUri != null ? targetUri : virtualhost + "__" + extractUpstreamField(exchange.getResponseHeaders(), targetUri);
            final boolean targetIsUndef = UNDEF.equals(targetUri);

            final Integer statusCode = exchange.getStatusCode();
            final String method = exchange.getRequestMethod().toString();
            final Integer responseTime = getResponseTime(exchange);
            final String key = cleanUpKey(virtualhost) + "." + cleanUpKey(targetUri);

            sendStatusCodeCount(key, statusCode, targetIsUndef);
            sendHttpMethodCount(key, method);
            sendResponseTime(key, responseTime, targetIsUndef);

            if (sendOpenconnCounter) {
                final Integer clientOpenConnection = exchange.getAttachment(ClientStatisticsMarker.TARGET_CONN);
                sendActiveConnCount(key, clientOpenConnection, targetIsUndef);
            }

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } finally {
            nextListener.proceed();
        }
    }

    private void sendStatusCodeCount(String key, Integer statusCode, boolean targetIsUndef) {
        int realStatusCode = targetIsUndef ? 503 : statusCode;
        String fullKey = key + ".status." + realStatusCode;
        statsdClient.incr(fullKey);
    }

    private void sendActiveConnCount(String key, Integer clientOpenConnection, boolean targetIsUndef) {
        int conn = (clientOpenConnection != null && !targetIsUndef) ? clientOpenConnection : 0;
        String fullKey = key + ".activeConns";
        statsdClient.gauge(fullKey, conn);
    }

    private void sendHttpMethodCount(String key, String method) {
        String fullKey = key + ".method." + method;
        statsdClient.count(fullKey, 1);
    }

    private void sendResponseTime(String key, long requestTime, boolean targetIsUndef) {
        long realRequestTime = targetIsUndef ? 0 : requestTime;
        String fullKey = key + ".responseTime";
        statsdClient.timing(fullKey, realRequestTime);
    }

    private int getResponseTime(HttpServerExchange exchange) {
        return Math.round(Float.parseFloat(responseTimeAttribute.readAttribute(exchange)));
    }

    private String cleanUpKey(String str) {
        return str.replaceAll("http://", "").replaceAll("[.:]", "_");
    }
}
