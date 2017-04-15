package io.galeb.router.configurations;

import io.galeb.router.handlers.NameVirtualHostDefaultHandler;
import io.galeb.router.services.ExternalDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NameVirtualHostDefaultHandlerConfiguration {

    private final ApplicationContext context;
    private final ExternalDataService data;

    @Autowired
    public NameVirtualHostDefaultHandlerConfiguration(final ApplicationContext context, final ExternalDataService externalData) {
        this.context = context;
        this.data = externalData;
    }

    @Bean
    public NameVirtualHostDefaultHandler nameVirtualHostDefaultHandler() {
        return new NameVirtualHostDefaultHandler(context, data);
    }

}
