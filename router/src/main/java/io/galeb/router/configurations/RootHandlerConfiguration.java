package io.galeb.router.configurations;

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RootHandlerConfiguration {

    private static final int SERVICE_UNAVAILABLE = 503;

    private final Log logger = LogFactory.getLog(this.getClass());

    private final NameVirtualHostHandler nameVirtualHostHandler;

    @Autowired
    public RootHandlerConfiguration(final NameVirtualHostHandler nameVirtualHostHandler) {
        this.nameVirtualHostHandler = nameVirtualHostHandler;
    }

    @Bean("rootHandler")
    public HttpHandler rootHandler() {
        return exchange -> {
            try {
                nameVirtualHostHandler.handleRequest(exchange);
            } catch (Exception e) {
                logger.error(ExceptionUtils.getStackTrace(e));
                exchange.setStatusCode(SERVICE_UNAVAILABLE);
            }
        };
    }
}
