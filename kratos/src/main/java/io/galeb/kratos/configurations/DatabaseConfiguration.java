package io.galeb.kratos.configurations;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EntityScan("io.galeb.core.entity")
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "io.galeb.kratos.repository")
public class DatabaseConfiguration {

    @Bean
    @Primary
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public HikariDataSource dataSource(DataSourceProperties properties) {
        return (HikariDataSource) properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }
}
