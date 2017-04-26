package io.galeb.router.configurations;

import io.galeb.core.services.HttpClientService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({ "production" })
public class HttpClientServiceConfiguration {

    @Bean
    HttpClientService httpClientService() {
        return new HttpClientService();
    }
}
