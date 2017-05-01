package io.galeb.router.configurations;

import io.galeb.router.sync.ManagerClient;
import io.galeb.router.sync.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ManagerClientConfiguration {

    @Bean
    ManagerClient managerClient(HttpClient httpClientService) {
        return new ManagerClient(httpClientService);
    }
}
