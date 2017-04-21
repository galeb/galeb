package io.galeb.router.handlers;

import io.galeb.router.ResponseCodeOnError;
import io.galeb.router.SystemEnvs;
import io.galeb.router.handlers.completionListeners.AccessLogCompletionListener;
import io.galeb.router.handlers.completionListeners.StatsdCompletionListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.NameVirtualHostHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RootHandler extends AbstractUpdater implements HttpHandler {

    private final Log logger = LogFactory.getLog(this.getClass());

    private final NameVirtualHostHandler nameVirtualHostHandler;
    private final AccessLogCompletionListener accessLogCompletionListener;
    private final StatsdCompletionListener statsdCompletionListener;

    private final boolean enableAccessLog = Boolean.parseBoolean(SystemEnvs.ENABLE_ACCESSLOG.getValue());
    private final boolean enableStatsd    = Boolean.parseBoolean(SystemEnvs.ENABLE_STATSD.getValue());

    public RootHandler(final NameVirtualHostHandler nameVirtualHostHandler,
                       final AccessLogCompletionListener accessLogCompletionListener,
                       final StatsdCompletionListener statsdCompletionListener) {
        super(nameVirtualHostHandler);
        this.nameVirtualHostHandler = nameVirtualHostHandler;
        this.accessLogCompletionListener = accessLogCompletionListener;
        this.statsdCompletionListener = statsdCompletionListener;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        try {
            if (enableAccessLog) exchange.addExchangeCompleteListener(accessLogCompletionListener);
            if (enableStatsd) exchange.addExchangeCompleteListener(statsdCompletionListener);

            nameVirtualHostHandler.handleRequest(exchange);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            ResponseCodeOnError.ROOT_HANDLER_FAILED.getHandler().handleRequest(exchange);
        }
    }

}
