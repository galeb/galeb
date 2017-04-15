package io.galeb.router.configurations;

import io.galeb.router.handlers.PoolHandler;
import io.galeb.router.services.ExternalDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class PoolHandlerConfiguration {

    private final ExternalDataService data;
    private final ApplicationContext context;

    @Autowired
    public PoolHandlerConfiguration(final ApplicationContext context, final ExternalDataService externalData) {
        this.context = context;
        this.data = externalData;
    }

    @Bean
    @Scope("prototype")
    PoolHandler poolHandler() {
        return new PoolHandler(context, data);
    }

}
