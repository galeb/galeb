package io.galeb.router.configurations;

import io.galeb.router.handlers.NameVirtualHostDefaultHandler;
import io.galeb.router.services.ExternalData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NameVirtualHostDefaultHandlerConfiguration {

    private final ApplicationContext context;
    private final ExternalData data;

    @Autowired
    public NameVirtualHostDefaultHandlerConfiguration(final ApplicationContext context, final ExternalData externalData) {
        this.context = context;
        this.data = externalData;
    }

    @Bean
    public NameVirtualHostDefaultHandler nameVirtualHostDefaultHandler() {
        return new NameVirtualHostDefaultHandler(context, data);
    }

}
