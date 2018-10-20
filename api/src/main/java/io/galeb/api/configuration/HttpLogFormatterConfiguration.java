package io.galeb.api.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.galeb.api.log.PrincipalHttpLogFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.JsonHttpLogFormatter;

@Configuration
public class HttpLogFormatterConfiguration {

    @Bean
    public HttpLogFormatter jsonFormatter(final ObjectMapper mapper) {
        return new PrincipalHttpLogFormatter(new JsonHttpLogFormatter(mapper));
    }

}
