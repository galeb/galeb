package io.galeb.kratos.configurations;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EntityScan("io.galeb.core.entity")
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "io.galeb.kratos.repository")
public class DatabaseConfiguration {
}
