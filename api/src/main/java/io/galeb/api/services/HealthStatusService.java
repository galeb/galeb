package io.galeb.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Set;

@Service
public class HealthStatusService {

    @Autowired
    StringRedisTemplate redisTemplate;

    private static final String FORMAT_KEY_HEALTH = "health:{0}:{1}:{2}";

    public int envWithStatusCount(Long envId) {
        String id = String.valueOf(envId);
        Set<String> keys = redisTemplate.keys(MessageFormat.format(FORMAT_KEY_HEALTH, id, "*", "*"));
        return keys == null ? 0 : keys.size();
    }
}
