package io.galeb.router.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.IPAddressAccessControlHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class RuleTargetHandler implements HttpHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AtomicBoolean loaded = new AtomicBoolean(false);
    private final HttpHandler next;
    private final ApplicationContext context;
    private String virtualHost;

    public RuleTargetHandler(final ApplicationContext context) {
        this.context = context;

        final PathGlobHandler pathGlobHandler = (PathGlobHandler) context.getBean("pathGlobHandler");
        this.next = hasAcl() ? loadAcl() : pathGlobHandler;
        pathGlobHandler.setDefaultHandler(loadRulesHandler(next));
    }

    public RuleTargetHandler setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
        return this;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        next.handleRequest(exchange);
    }

    private HttpHandler loadRulesHandler(HttpHandler next) {
        return new HttpHandler() {

            final PathGlobHandler pathGlobHandler = next instanceof PathGlobHandler ? (PathGlobHandler) next : (PathGlobHandler) ((IPAddressAccessControlHandler) next).getNext();

            @Override
            public synchronized void handleRequest(HttpServerExchange exchange) throws Exception {
                if (!loaded.get()) {
                    loadRules(virtualHost);
                    next.handleRequest(exchange);
                } else {
                    ResponseCodeHandler.HANDLE_500.handleRequest(exchange);
                }
            }

            private void loadRules(String virtualHost) {
                logger.info("Added rule /");
                String poolname = "pool1";
                final PoolHandler poolHandler = ((PoolHandler) context.getBean("poolHandler")).setPoolName(poolname);
                pathGlobHandler.addPath("/", 0, poolHandler);
                loaded.set(true);
            }
        };
    }

    private boolean hasAcl() {
        return false;
    }

    private HttpHandler loadAcl() {
        final IPAddressAccessControlHandler ipAddressAccessControlHandler = new IPAddressAccessControlHandler().setNext(next);
        Arrays.asList("127.0.0.1", "10.0.0.0/8").forEach(ipAddressAccessControlHandler::addAllow);
        return ipAddressAccessControlHandler;
    }
}
