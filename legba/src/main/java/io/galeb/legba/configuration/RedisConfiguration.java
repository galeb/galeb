package io.galeb.legba.configuration;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class RedisConfiguration {

    @Bean
    @Primary
    public RedisProperties redisProperties() {
        return new RedisProperties();
    }

    @Bean
    public RedisConnectionFactory lettuceConnectionFactory(RedisProperties redisProperties) {
        if (redisProperties.getSentinel() != null) {
            Set nodesSentinel = Arrays.stream(redisProperties.getSentinel().getNodes().split(",")).collect(Collectors.toSet());
            RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration(redisProperties.getSentinel().getMaster(), nodesSentinel);
            return new LettuceConnectionFactory(redisSentinelConfiguration);
        } else {
            return new LettuceConnectionFactory(redisProperties.getHost(), redisProperties.getPort());
        }

    }

    @Bean(name = "redisTemplate")
    public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

}
