package io.galeb.api.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DatabaseConfiguration {

    private static String url;
    private static String driver;
    private static String username;
    private static String password;
    static {
        String driverEnvName = System.getProperty("io.galeb.api.datasource.driver.env", "GALEB_DB_DRIVER");
        driver = System.getenv(driverEnvName);
        driver = driver != null ? driver : "org.h2.Driver";

        String urlEnvName = System.getProperty("io.galeb.api.datasource.url.env", "GALEB_DB_URL");
        url = System.getenv(urlEnvName);
        url = url != null ? url : "jdbc:h2:mem:galeb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";

        String usernameEnvName = System.getProperty("io.galeb.api.datasource.username.env", "GALEB_DB_USER");
        username = System.getenv(usernameEnvName);
        username = username != null ? username : "root";

        String passwordEnvName = System.getProperty("io.galeb.api.datasource.password.env", "GALEB_DB_PASS");
        password = System.getenv(passwordEnvName);
        password = password != null ? password : "";
    }

    @Bean
    @Primary
    @ConfigurationProperties("api.datasource")
    public DataSourceProperties dataSourceProperties() {
        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        dataSourceProperties.setUrl(getUrl());
        dataSourceProperties.setUsername(getUsername());
        dataSourceProperties.setPassword(getPassword());
        dataSourceProperties.setDriverClassName(getDriver());
        dataSourceProperties.setType(HikariDataSource.class);
        return dataSourceProperties;
    }

    @Bean
    @ConfigurationProperties("api.datasource")
    public HikariDataSource dataSource(DataSourceProperties properties) {
        return (HikariDataSource) properties.initializeDataSourceBuilder().build();
    }

    public static String getUrl() {
        return url;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    public static String getDriver() {
        return driver;
    }

}
