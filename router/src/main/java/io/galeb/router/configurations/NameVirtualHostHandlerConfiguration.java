package io.galeb.router.configurations;

import io.galeb.router.handlers.NameVirtualHostDefaultHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NameVirtualHostHandlerConfiguration {

    private final HttpHandler nameVirtualHostDefaultHandler;

    @Autowired
    public NameVirtualHostHandlerConfiguration(final NameVirtualHostDefaultHandler nameVirtualHostDefaultHandler) {
        this.nameVirtualHostDefaultHandler = nameVirtualHostDefaultHandler;
    }

    @Bean
    NameVirtualHostHandler nameVirtualHostHandler() {
        return new NameVirtualHostHandler().setDefaultHandler(nameVirtualHostDefaultHandler);
    }

}
