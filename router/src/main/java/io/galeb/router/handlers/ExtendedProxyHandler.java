/**
 *
 */
package io.galeb.router.handlers;

import io.galeb.router.ResponseCodeOnError;
import io.galeb.router.SystemEnvs;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
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

    private final int maxRequestTime = Integer.parseInt(SystemEnvs.POOL_MAX_REQUESTS.getValue());
    private final boolean reuseXForwarded = Boolean.parseBoolean(SystemEnvs.REUSE_XFORWARDED.getValue());
    private final boolean rewriteHostHeader = Boolean.parseBoolean(SystemEnvs.REWRITE_HOST_HEADER.getValue());

    private ProxyHandler proxyHandler;

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (proxyHandler != null) {
            proxyHandler.handleRequest(exchange);
        } else {
            logger.error("proxyHandler is NULL");
            ResponseCodeOnError.PROXY_HANDLER_NOT_DEFINED.getHandler().handleRequest(exchange);
        }
    }

    public ExtendedProxyHandler setProxyClientAndDefaultHandler(final ProxyClient proxyClient, final HttpHandler defaultHandler) {
        proxyHandler = new ProxyHandler(proxyClient, maxRequestTime, defaultHandler, rewriteHostHeader, reuseXForwarded);
        return this;
    }

}
