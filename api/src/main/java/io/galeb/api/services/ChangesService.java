package io.galeb.api.services;

import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChangesService {

    /**
     * Description arguments:
     * {0} - Environment Id
     * {1} - Entity Simple Name Class
     * {2} - Entity Id
     * {3} - Entity Last Modified At
     */
    private static final String FORMAT_KEY_HAS_CHANGE = "haschange:{0}:{1}:{2}:{3}";

    @Autowired
    StringRedisTemplate redisTemplate;

    public void register(Environment e, AbstractEntity entity, String version) {
        String simpleNameClass = entity.getClass().getSimpleName().toLowerCase();
        String envId = String.valueOf(e.getId());
        Long entityId = entity.getId();
        String entityLastModifiedAt = String.valueOf(entity.getLastModifiedAt().getTime());
        String keyFormatted = MessageFormat.format(FORMAT_KEY_HAS_CHANGE, envId, simpleNameClass, entityId, entityLastModifiedAt);
        redisTemplate.opsForValue().setIfAbsent(keyFormatted, version);
    }

    public boolean hasByEnvironmentId(Long environmentId) {
        String keyFormatted = MessageFormat.format(FORMAT_KEY_HAS_CHANGE, environmentId, "*", "*", "*");
        return hasKey(keyFormatted);
    }

    public Set<Long> listEnvironmentIds(AbstractEntity entity) {
        String simpleNameClass = entity.getClass().getSimpleName().toLowerCase();
        Long entityId = entity.getId();
        String keyFormatted = MessageFormat.format(FORMAT_KEY_HAS_CHANGE, "*",simpleNameClass, entityId, "*");
        Set<String> keys = keys(keyFormatted);
        return keys.stream().map(k -> Long.parseLong(new MessageFormat(FORMAT_KEY_HAS_CHANGE).parse(k, new ParsePosition(0))[0].toString())).collect(Collectors.toSet());
    }

    private boolean hasKey(String key) {
        final Set<String> result = keys(key);
        return result != null && !result.isEmpty();
    }

    private Set<String> keys(String key) {
        return redisTemplate.keys(key);
    }

}
