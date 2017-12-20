package io.galeb.api.services;

import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChangesService {

    private static final String PREFIX_VERSION     = "version:";
    private static final String PREFIX_HAS_CHANGE  = "haschange:";

    @Autowired
    RedisTemplate redisTemplate;

    public void register(Environment e, AbstractEntity entity) {
        long envId = e.getId();
        Long newVersion = incrementVersion(envId);
        String suffix = entity.getClass().getSimpleName().toLowerCase() + ":" + entity.getId() + ":" + entity.getLastModifiedAt().getTime();
        redisTemplate.opsForValue().setIfAbsent(PREFIX_HAS_CHANGE + envId + ":" + suffix, String.valueOf(newVersion));
    }

    private Long incrementVersion(long envId) {
        return redisTemplate.opsForValue().increment(PREFIX_VERSION + envId, 1);
    }
}
