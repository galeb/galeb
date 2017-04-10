package io.galeb.router.configurations;

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NameVirtualHostHandlerConfiguration {

    private final HttpHandler nameVirtualHostHandlerDefault;

    @Autowired
    public NameVirtualHostHandlerConfiguration(@Value("#{nameVirtualHostHandlerDefault}") final HttpHandler nameVirtualHostHandlerDefault) {
        this.nameVirtualHostHandlerDefault = nameVirtualHostHandlerDefault;
    }

    @Bean
    NameVirtualHostHandler nameVirtualHostHandler() {
        return new NameVirtualHostHandler().setDefaultHandler(nameVirtualHostHandlerDefault);
    }

}
