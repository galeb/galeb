/**
 *
 */
package io.galeb.router.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.proxy.ProxyClient;
import io.undertow.server.handlers.proxy.ProxyHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ExtendedProxyHandler implements HttpHandler {

    private final Log logger = LogFactory.getLog(this.getClass());

    private ProxyHandler proxyHandler;

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (proxyHandler != null) {
            proxyHandler.handleRequest(exchange);
        } else {
            logger.error("proxyHandler is NULL");
            ResponseCodeHandler.HANDLE_500.handleRequest(exchange);
        }
    }

    public ExtendedProxyHandler setProxyClientAndDefaultHandler(final ProxyClient proxyClient, final HttpHandler defaultHandler) {
        proxyHandler = new ProxyHandler(proxyClient, defaultHandler);
        return this;
    }

}
