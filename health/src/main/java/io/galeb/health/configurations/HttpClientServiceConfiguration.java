package io.galeb.health.configurations;

import io.galeb.core.services.HttpClientService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientServiceConfiguration {

    @Bean
    HttpClientService httpClientService() {
        return new HttpClientService();
    }
}
