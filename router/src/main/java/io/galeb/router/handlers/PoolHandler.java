package io.galeb.router.handlers;

import io.galeb.router.client.ExtendedLoadBalancingProxyClient;
import io.galeb.router.client.hostselectors.HostSelector;
import io.galeb.router.client.hostselectors.HostSelectorAlgorithm;
import io.galeb.router.services.ExternalData;
import io.undertow.client.UndertowClient;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.zalando.boot.etcd.EtcdNode;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.galeb.router.client.hostselectors.HostSelectorAlgorithm.ROUNDROBIN;
import static io.galeb.router.services.ExternalData.POOLS_KEY;

public class PoolHandler implements HttpHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HttpHandler defaultHandler;
    private final ExternalData data;
    private final ApplicationContext context;

    private ExtendedProxyHandler proxyHandler = null;
    private String poolname = null;
    private final AtomicBoolean loaded = new AtomicBoolean(false);
    private final String checkRuleHeader = "X-Check-Pool";

    public PoolHandler(final ApplicationContext context, final ExternalData externalData) {
        this.context = context;
        this.data = externalData;
        this.defaultHandler = buildPoolHandler();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.getRequestHeaders().contains(checkRuleHeader)) {
            healthcheckPoolHandler().handleRequest(exchange);
            return;
        }
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
                    HostSelector hostSelector = defineHostSelector();
                    logger.info("[Pool " + poolname + "] HostSelector: " + hostSelector.getClass().getSimpleName());
                    final ExtendedLoadBalancingProxyClient proxyClient = new ExtendedLoadBalancingProxyClient(UndertowClient.getInstance(),
                                        exclusivityCheckerExchange -> exclusivityCheckerExchange.getRequestHeaders().contains(Headers.UPGRADE), hostSelector)
                                    .setConnectionsPerThread(2000);
                    addTargets(proxyClient);
                    proxyHandler = context.getBean(ExtendedProxyHandler.class)
                            .setProxyClientAndDefaultHandler(proxyClient, badGatewayHandler());
                    proxyHandler.handleRequest(exchange);
                    return;
                }
                ResponseCodeHandler.HANDLE_500.handleRequest(exchange);
            }
        };
    }

    private HttpHandler badGatewayHandler() {
        return exchange -> exchange.setStatusCode(502);
    }

    private HostSelector defineHostSelector() {
        if (poolname != null) {
            final String hostSelectorKeyName = POOLS_KEY + "/" + poolname + "/loadbalance";
            final EtcdNode hostSelectorNode = data.node(hostSelectorKeyName);
            if (hostSelectorNode.getKey() != null) {
                String hostSelectorName = hostSelectorNode.getValue();
                return HostSelectorAlgorithm.valueOf(hostSelectorName).getHostSelector();
            }
        }
        return ROUNDROBIN.getHostSelector();
    }

    private void addTargets(final ExtendedLoadBalancingProxyClient proxyClient) {
        if (poolname != null) {
            final String poolNameKey = POOLS_KEY + "/" + poolname + "/targets";
            for (EtcdNode etcdNode : data.listFrom(poolNameKey)) {
                String value = etcdNode.getValue();
                URI uri = URI.create(value);
                proxyClient.addHost(uri);
                logger.info("added target " + value);
            }
        }
    }

    private HttpHandler healthcheckPoolHandler() {
        return exchange -> {
            logger.warn("detected header " + checkRuleHeader);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            exchange.getResponseHeaders().put(Headers.SERVER, "GALEB");
            exchange.getResponseHeaders().put(HttpString.tryFromString("X-Pool-Name"), poolname);
            exchange.getResponseSender().send("POOL_REACHABLE");
        };
    }
}
