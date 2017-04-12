package io.galeb.router.configurations;

import io.galeb.router.completionListeners.AccessLogCompletionListener;
import io.galeb.router.completionListeners.StatsdCompletionListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.util.Headers;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RootHandlerConfiguration {

    private static final int SERVICE_UNAVAILABLE = 503;

    private final Log logger = LogFactory.getLog(this.getClass());

    private final NameVirtualHostHandler nameVirtualHostHandler;
    private final AccessLogCompletionListener accessLogCompletionListener;
    private final StatsdCompletionListener statsdCompletionListener;

    @Autowired
    public RootHandlerConfiguration(final NameVirtualHostHandler nameVirtualHostHandler,
                                    final AccessLogCompletionListener accessLogCompletionListener,
                                    final StatsdCompletionListener statsdCompletionListener) {
        this.nameVirtualHostHandler = nameVirtualHostHandler;
        this.accessLogCompletionListener = accessLogCompletionListener;
        this.statsdCompletionListener = statsdCompletionListener;
        nameVirtualHostHandler.addHost("__ping__", pingHandler());
    }

    private HttpHandler pingHandler() {
        return exchange -> {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            exchange.getResponseHeaders().put(Headers.SERVER, "GALEB");
            exchange.getResponseSender().send("WORKING");
        };
    }

    @Bean("rootHandler")
    public HttpHandler rootHandler() {
        return exchange -> {
            try {
                exchange.addExchangeCompleteListener(accessLogCompletionListener);
                exchange.addExchangeCompleteListener(statsdCompletionListener);
                nameVirtualHostHandler.handleRequest(exchange);
            } catch (Exception e) {
                logger.error(ExceptionUtils.getStackTrace(e));
                exchange.setStatusCode(SERVICE_UNAVAILABLE);
            }
        };
    }
}
