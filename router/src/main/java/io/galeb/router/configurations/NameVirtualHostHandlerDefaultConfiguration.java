package io.galeb.router.configurations;

import io.galeb.router.handlers.RuleTargetHandler;
import io.galeb.router.services.ExternalData;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import static io.galeb.router.services.ExternalData.VIRTUALHOSTS_KEY;

@Configuration
public class NameVirtualHostHandlerDefaultConfiguration {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Set<String> hostNames = new ConcurrentSkipListSet<>();
    private final ApplicationContext context;
    private final ExternalData data;

    @Autowired
    public NameVirtualHostHandlerDefaultConfiguration(final ApplicationContext context, final ExternalData externalData) {
        this.context = context;
        this.data = externalData;
    }

    @Bean("nameVirtualHostHandlerDefault")
    public HttpHandler nameVirtualHostHandlerDefault() {
        return exchange -> {
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
        };
    }

    private synchronized boolean isValid(String hostName) {
        final String virtualhostNodeName = VIRTUALHOSTS_KEY + "/" + hostName;
        return data.exist(virtualhostNodeName) && hostNames.add(hostName);
    }

}
