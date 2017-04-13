package io.galeb.router.configurations;

import io.galeb.router.completionListeners.AccessLogCompletionListener;
import io.galeb.router.completionListeners.StatsdCompletionListener;
import io.galeb.router.handlers.RootHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.util.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RootHandlerConfiguration {

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

    @Bean
    public RootHandler rootHandler() {
        return new RootHandler(nameVirtualHostHandler, accessLogCompletionListener, statsdCompletionListener);
    }

}
