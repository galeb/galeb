package io.galeb.legba.services;

import io.galeb.core.services.VersionService;
import io.galeb.legba.common.ErrorLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RoutersService {

    /**
     * Description arguments:
     * {0} - Environment Id
     * {1} - Group ID
     * {2} - Local IP
     */
    private static final String FORMAT_KEY_VERSION = "routers:{0}:{1}:{2}";

    public static final long REGISTER_TTL  = Long.valueOf(Optional.ofNullable(System.getenv("REGISTER_ROUTER_TTL")).orElse("30000")); // ms

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    private VersionService versionService;

    public Set<JsonSchema.Env> get() {
        return get(null);
    }

    public Set<JsonSchema.Env> get(String environmentId) {
        try {
            Assert.notNull(redisTemplate, StringRedisTemplate.class.getSimpleName() + " IS NULL");

            final Set<JsonSchema.Env> envs = new HashSet<>();
            String key_envid = environmentId == null ? "*" : environmentId;
            redisTemplate.keys(MessageFormat.format(FORMAT_KEY_VERSION, key_envid, "*", "*")).forEach(key -> {
                String etag = redisTemplate.opsForValue().get(key);
                long expire = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
                MessageFormat ms = new MessageFormat(FORMAT_KEY_VERSION);
                String env = (String)ms.parse(key, new ParsePosition(0))[0];;
                String groupId = (String)ms.parse(key, new ParsePosition(1))[0];;
                String localIp = (String)ms.parse(key, new ParsePosition(2))[0];

                JsonSchema.Env envSchema = envs.stream()
                        .filter(e -> e.getEnvId().equals(env))
                        .findAny()
                        .orElseGet(() -> new JsonSchema.Env(env, new HashSet<>()));
                JsonSchema.GroupID groupIDSchema = envSchema.getGroupIDs().stream()
                        .filter(g -> g.getGroupID().equals(groupId))
                        .findAny()
                        .orElseGet(() -> new JsonSchema.GroupID(groupId, new HashSet<>()));
                groupIDSchema.getRouters().add(new JsonSchema.Router(localIp, etag, expire));
                envSchema.getGroupIDs().add(groupIDSchema);
                envs.add(envSchema);
            });
            return envs;
        } catch (Exception e) {
            ErrorLogger.logError(e, this.getClass());
        }
        return Collections.emptySet();
    }

    public String get(String envid, String routerGroupId) {
//        int numRouters;
//        numRouters = routerMap.get(envname)
//                .stream()
//                .mapToInt(e -> e.getGroupIDs()
//                        .stream()
//                        .filter(g -> g.getGroupID().equals(routerGroupIp))
//                        .mapToInt(r -> r.getRouters().size())
//                        .sum())
//                .sum();
//        return numRouters;

        return "0";
    }

    public void put(String routerGroupId, String routerLocalIP, String version, String envid) {
        try {
            String key = MessageFormat.format(FORMAT_KEY_VERSION, envid, routerGroupId, routerLocalIP);
            Assert.notNull(redisTemplate, StringRedisTemplate.class.getSimpleName() + " IS NULL");
            if (!redisTemplate.hasKey(key)) {
                versionService.incrementVersion(Long.valueOf(envid));
            }
            redisTemplate.opsForValue().set(key, version, REGISTER_TTL, TimeUnit.MILLISECONDS);
            //routerState.updateRouterState(envid);
        } catch (Exception e) {
            ErrorLogger.logError(e, this.getClass());
        }

    }
}
