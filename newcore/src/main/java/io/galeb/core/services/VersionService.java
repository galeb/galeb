package io.galeb.core.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

@Service
public class VersionService {

    /**
     * Description arguments:
     * {0} - Environment Id
     */
    private static final String FORMAT_KEY_VERSION = "version:{0}";

    /**
     * Description arguments:
     * {0} - Environment Id
     * {1} - Version number
     */
    private static final String FORMAT_KEY_CACHE = "cache:{0}:{1}";

    @Autowired
    StringRedisTemplate redisTemplate;

    public String getActualVersion(String envid) {
        String version = redisTemplate.opsForValue().get(MessageFormat.format(FORMAT_KEY_VERSION, envid));
        if (version == null) {
            version = String.valueOf(redisTemplate.opsForValue().increment(MessageFormat.format(FORMAT_KEY_VERSION, envid), 1));
        }
        return version;
    }

    public String getCache(String envid, String version) {
        return redisTemplate.opsForValue().get(MessageFormat.format(FORMAT_KEY_CACHE, envid, version));
    }

    public void setCache(String cache, String envid, String version) {
        redisTemplate.opsForValue().set(MessageFormat.format(FORMAT_KEY_CACHE, envid, version), cache, 5, TimeUnit.MINUTES);
    }

    public Long incrementVersion(long envId) {
        String keyFormatted = MessageFormat.format(FORMAT_KEY_VERSION, envId);
        return redisTemplate.opsForValue().increment(keyFormatted, 1);
    }

}

