package io.galeb.router.configurations;

import io.galeb.router.handlers.completionListeners.AccessLogCompletionListener;
import io.galeb.router.handlers.completionListeners.StatsdCompletionListener;
import io.galeb.router.handlers.RootHandler;
import io.galeb.router.services.ExternalDataService;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.util.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@Order(1)
public class RootHandlerConfiguration {

    private final NameVirtualHostHandler nameVirtualHostHandler;
    private final AccessLogCompletionListener accessLogCompletionListener;
    private final StatsdCompletionListener statsdCompletionListener;
    private final ExternalDataService data;

    @Autowired
    public RootHandlerConfiguration(final NameVirtualHostHandler nameVirtualHostHandler,
                                    final AccessLogCompletionListener accessLogCompletionListener,
                                    final StatsdCompletionListener statsdCompletionListener,
                                    final ExternalDataService externalData) {
        this.nameVirtualHostHandler = nameVirtualHostHandler;
        this.accessLogCompletionListener = accessLogCompletionListener;
        this.statsdCompletionListener = statsdCompletionListener;
        data = externalData;
        nameVirtualHostHandler.addHost("__ping__", pingHandler());
    }

    private HttpHandler pingHandler() {
        return exchange -> {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            exchange.getResponseHeaders().put(Headers.SERVER, "GALEB");
            exchange.getResponseSender().send((data.exist(ExternalDataService.PREFIX_KEY) ? "WORKING" : "FAIL: " + ExternalDataService.PREFIX_KEY + " not found"));
        };
    }

    @Bean
    public RootHandler rootHandler() {
        return new RootHandler(nameVirtualHostHandler, accessLogCompletionListener, statsdCompletionListener);
    }

}
