package io.galeb.health.configurations;

import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;

import javax.jms.ConnectionFactory;

@Configuration
@EnableJms
public class JMSConfiguration {

    @Bean
    public DefaultJmsListenerContainerFactory containerFactory(DefaultJmsListenerContainerFactoryConfigurer configurer, ConnectionFactory targetConnectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(targetConnectionFactory);
        cachingConnectionFactory.setSessionCacheSize(100);
        cachingConnectionFactory.setCacheConsumers(true);
        configurer.configure(factory, cachingConnectionFactory);
        return factory;
    }

}
