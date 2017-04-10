package io.galeb.router.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.stream.Stream;

public class PoolHandler implements HttpHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HttpHandler defaultHandler;

    private ProxyHandler proxyHandler = null;
    private String poolname = null;

    public PoolHandler() {
        this.defaultHandler = buildPoolHandler();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (proxyHandler != null) {
            proxyHandler.handleRequest(exchange);
        } else {
            defaultHandler.handleRequest(exchange);
        }
    }

    PoolHandler setPoolName(String aPoolname) {
        poolname = aPoolname;
        return this;
    }

    private HttpHandler buildPoolHandler() {
        return exchange -> {
            if (poolname != null) {
                logger.info("creating pool " + poolname);
                final LoadBalancingProxyClient proxyClient = new LoadBalancingProxyClient().setConnectionsPerThread(2000);
                addTargets(proxyClient);
                proxyHandler = new ProxyHandler(proxyClient, ResponseCodeHandler.HANDLE_500);
                proxyHandler.handleRequest(exchange);
                return;
            }
            ResponseCodeHandler.HANDLE_500.handleRequest(exchange);
        };
    }

    private void addTargets(final LoadBalancingProxyClient proxyClient) {
        Stream.of("http://127.0.0.1:8080").map(URI::create).forEach(proxyClient::addHost);
    }
}
