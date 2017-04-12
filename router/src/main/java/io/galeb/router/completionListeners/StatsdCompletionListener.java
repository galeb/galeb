package io.galeb.router.completionListeners;

import io.galeb.router.client.hostselectors.ClientStatisticsMarker;
import io.galeb.router.client.hostselectors.HostSelector;
import io.galeb.router.services.StatsdClient;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StatsdCompletionListener implements ExchangeCompletionListener, ProcessorLocalStatusCode {

    private static final String UNDEF = "UNDEF";

    private final Log logger = LogFactory.getLog(this.getClass());

    private final StatsdClient statsdClient;

    @Autowired
    public StatsdCompletionListener(StatsdClient statsdClient) {
        this.statsdClient = statsdClient;
    }

    @Override
    public void exchangeEvent(HttpServerExchange exchange, NextListener nextListener) {
        try {
            String virtualhost = exchange.getHostName();
            virtualhost = virtualhost != null ? virtualhost : UNDEF;
            String targetUri = exchange.getAttachment(HostSelector.REAL_DEST);
            targetUri = targetUri != null ? targetUri : virtualhost + "__" + UNDEF;
            final boolean targetIsUndef = UNDEF.equals(targetUri);

            final Integer statusCode = exchange.getStatusCode();
            final Integer clientOpenConnection = exchange.getAttachment(ClientStatisticsMarker.TARGET_CONN);
            final String method = exchange.getRequestMethod().toString();
            final Integer responseTime = getResponseTime(exchange);

            final String key = cleanUpKey(virtualhost) + "." + cleanUpKey(targetUri);

            sendStatusCodeCount(key, statusCode, targetIsUndef);
            sendActiveConnCount(key, clientOpenConnection, targetIsUndef);
            sendHttpMethodCount(key, method);
            sendResponseTime(key, responseTime, targetIsUndef);

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
