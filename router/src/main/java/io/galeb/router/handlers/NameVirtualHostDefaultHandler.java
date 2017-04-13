package io.galeb.router.handlers;

import io.galeb.router.configurations.ResponseCodeOnError;
import io.galeb.router.services.ExternalData;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.NameVirtualHostHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import static io.galeb.router.services.ExternalData.VIRTUALHOSTS_KEY;

public class NameVirtualHostDefaultHandler implements HttpHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ApplicationContext context;
    private final ExternalData data;

    public NameVirtualHostDefaultHandler(final ApplicationContext context, final ExternalData externalData) {
        this.context = context;
        this.data = externalData;
    }

    @Override
    public synchronized void handleRequest(HttpServerExchange exchange) throws Exception {
        if (!data.exist(VIRTUALHOSTS_KEY)) {
            logger.error(VIRTUALHOSTS_KEY + " not found");
            ResponseCodeOnError.ETCD_VIRTUALHOSTS_PATH_NOT_FOUND.getHandler().handleRequest(exchange);
            return;
        }
        final String hostName = exchange.getHostName();
        final NameVirtualHostHandler nameVirtualHostHandler = (NameVirtualHostHandler) context.getBean("nameVirtualHostHandler");
        if (isValid(hostName, nameVirtualHostHandler)) {
            logger.info("adding " + hostName);
            nameVirtualHostHandler.addHost(hostName, ((RuleTargetHandler) context.getBean("ruleTargetHandler")).setVirtualHost(hostName));
            nameVirtualHostHandler.handleRequest(exchange);
        } else {
            ResponseCodeOnError.VIRTUALHOST_NOT_FOUND.getHandler().handleRequest(exchange);
        }
    }

    private synchronized boolean isValid(String hostName, final NameVirtualHostHandler nameVirtualHostHandler) {
        final String virtualhostNodeKey = VIRTUALHOSTS_KEY + "/" + hostName;
        return data.exist(virtualhostNodeKey) && !nameVirtualHostHandler.getHosts().containsKey(virtualhostNodeKey);
    }
}
