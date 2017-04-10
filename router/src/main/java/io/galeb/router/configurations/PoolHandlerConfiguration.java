package io.galeb.router.configurations;

import io.galeb.router.handlers.PoolHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class PoolHandlerConfiguration {

    @Bean
    @Scope("prototype")
    PoolHandler poolHandler() {
        return new PoolHandler();
    }

}
