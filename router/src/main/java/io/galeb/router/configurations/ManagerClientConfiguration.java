package io.galeb.router.configurations;

import io.galeb.core.rest.ManagerClient;
import io.galeb.core.services.HttpClientService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ManagerClientConfiguration {

    @Bean
    ManagerClient managerClient(HttpClientService httpClientService) {
        return new ManagerClient(httpClientService);
    }
}
