package io.galeb.router.configurations;

import io.galeb.router.handlers.PoolHandler;
import io.galeb.router.services.ExternalDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class PoolHandlerConfiguration {

    private final ExternalDataService data;

    @Autowired
    public PoolHandlerConfiguration(final ExternalDataService externalData) {
        this.data = externalData;
    }

    @Bean
    @Scope("prototype")
    PoolHandler poolHandler() {
        return new PoolHandler(data);
    }

}
