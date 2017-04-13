package io.galeb.router.handlers;

import io.galeb.router.services.ExternalData;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import static io.galeb.router.services.ExternalData.VIRTUALHOSTS_KEY;

public class NameVirtualHostDefaultHandler implements HttpHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Set<String> hostNames = new ConcurrentSkipListSet<>();

    private final ApplicationContext context;
    private final ExternalData data;

    public NameVirtualHostDefaultHandler(final ApplicationContext context, final ExternalData externalData) {
        this.context = context;
        this.data = externalData;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (!data.exist(VIRTUALHOSTS_KEY)) {
            logger.error(VIRTUALHOSTS_KEY + " not found");
            ResponseCodeHandler.HANDLE_500.handleRequest(exchange);
            return;
        }
        final String hostName = exchange.getHostName();
        if (isValid(hostName)) {
            logger.info("adding " + hostName);
            NameVirtualHostHandler nameVirtualHostHandler = (NameVirtualHostHandler) context.getBean("nameVirtualHostHandler");
            nameVirtualHostHandler.addHost(hostName, ((RuleTargetHandler) context.getBean("ruleTargetHandler")).setVirtualHost(hostName));
            nameVirtualHostHandler.handleRequest(exchange);
        } else {
            ResponseCodeHandler.HANDLE_500.handleRequest(exchange);
        }
    }

    public synchronized void forgetIt(String hostName) {
        hostNames.remove(hostName);
    }

    private synchronized boolean isValid(String hostName) {
        final String virtualhostNodeName = VIRTUALHOSTS_KEY + "/" + hostName;
        return data.exist(virtualhostNodeName) && hostNames.add(hostName);
    }
}
