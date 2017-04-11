package io.galeb.router.handlers;

import io.galeb.router.services.ExternalData;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.boot.etcd.EtcdNode;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.galeb.router.services.ExternalData.POOLS_KEY;

public class PoolHandler implements HttpHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HttpHandler defaultHandler;
    private final ExternalData data;

    private ProxyHandler proxyHandler = null;
    private String poolname = null;
    private final AtomicBoolean loaded = new AtomicBoolean(false);

    public PoolHandler(final ExternalData externalData) {
        this.data = externalData;
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
            synchronized (loaded) {
                loaded.set(true);
                if (poolname != null) {
                    logger.info("creating pool " + poolname);
                    final LoadBalancingProxyClient proxyClient = new LoadBalancingProxyClient().setConnectionsPerThread(2000);
                    addTargets(proxyClient);
                    proxyHandler = new ProxyHandler(proxyClient, ResponseCodeHandler.HANDLE_500);
                    proxyHandler.handleRequest(exchange);
                    return;
                }
                ResponseCodeHandler.HANDLE_500.handleRequest(exchange);
            }
        };
    }

    private void addTargets(final LoadBalancingProxyClient proxyClient) {
        if (poolname != null) {
            final String poolNameKey = POOLS_KEY + "/" + poolname + "/targets";
            data.listFrom(poolNameKey).stream().map(EtcdNode::getValue).map(URI::create).forEach(proxyClient::addHost);
        }
    }
}
