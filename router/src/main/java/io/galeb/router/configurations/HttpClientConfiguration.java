package io.galeb.router.configurations;

import io.galeb.router.sync.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({ "production" })
public class HttpClientConfiguration {

    @Bean
    HttpClient httpClientService() {
        return new HttpClient();
    }
}
