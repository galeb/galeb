/*
 * Copyright (c) 2014-2018 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.legba.services;

import io.galeb.core.common.EntitiesRegistrable;
import io.galeb.core.common.HasChangeData;
import io.galeb.core.enums.SystemEnv;
import io.galeb.core.log.JsonEventToLogger;
import io.galeb.core.services.ChangesService;
import io.galeb.core.services.VersionService;
import io.galeb.legba.controller.RoutersController.RouterMeta;
import io.galeb.legba.conversors.Converter;
import io.galeb.legba.conversors.ConverterV1;
import io.galeb.legba.conversors.ConverterV2;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

@Service
public class RoutersService {

    /**
     * Description arguments:
     * {0} - Environment Id
     * {1} - Group ID
     * {2} - Local IP
     */
    private static final String FORMAT_KEY_ROUTERS = "routers:{0}:{1}:{2}";

    private static final String ROUTER_MAP_CACHE_REBUILD_LOCK = "router-map-cache-rebuild-lock";

    private static long REGISTER_TTL = Long.parseLong(SystemEnv.REGISTER_ROUTER_TTL.getValue());

    private static long ROUTER_CONCURRENT_LOCK_TTL = 120000L; // Long.parseLong(SystemEnv.ROUTER_CONCURRENT_LOCK_TTL.getValue())

    private static String DEFAULT_API_VERSION = ConverterV1.API_VERSION;

    private final StringRedisTemplate redisTemplate;
    private final ChangesService changesService;
    private final VersionService versionService;
    private final ConverterV1 converterV1;
    private final ConverterV2 converterV2;
    private final EntityManagerFactory entityManagerFactory;

    @Autowired
    public RoutersService(StringRedisTemplate redisTemplate, ChangesService changesService,
        VersionService versionService, ConverterV1 converterV1, ConverterV2 converterV2,
        EntityManagerFactory entityManagerFactory) {
        this.redisTemplate = redisTemplate;
        this.changesService = changesService;
        this.versionService = versionService;
        this.converterV1 = converterV1;
        this.converterV2 = converterV2;
        this.entityManagerFactory = entityManagerFactory;
    }

    public Set<JsonSchema.Env> get() {
        return get(null);
    }

    public Set<JsonSchema.Env> get(String environmentId) {
        final JsonEventToLogger event = new JsonEventToLogger(this.getClass());

        try {
            final Set<JsonSchema.Env> envs = new HashSet<>();
            String keyEnvId = environmentId == null ? "*" : environmentId;
            redisTemplate.keys(MessageFormat.format(FORMAT_KEY_ROUTERS, keyEnvId, "*", "*")).forEach(key -> {
                try {
                    String etag = redisTemplate.opsForValue().get(key);
                    long expire = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
                    MessageFormat ms = new MessageFormat(FORMAT_KEY_ROUTERS);
                    Object[] positions = ms.parse(key);
                    String env = (String) positions[0];
                    String groupId = (String) positions[1];
                    String localIp = (String) positions[2];

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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return envs;
        } catch (Exception e) {
            event.put("message", "GET /routers/" + environmentId + " FAILED");
            event.sendError(e);
        }
        return Collections.emptySet();
    }

    public int get(String envid, String routerGroupId) {
        int numRouters;
        numRouters = get(envid)
                .stream()
                .mapToInt(e -> e.getGroupIDs()
                        .stream()
                        .filter(g -> g.getGroupID().equals(routerGroupId))
                        .mapToInt(r -> r.getRouters().size())
                        .sum())
                .sum();
        return numRouters;
    }

    public void put(final RouterMeta routerMeta) {
        final JsonEventToLogger event = new JsonEventToLogger(this.getClass());
        String logCorrelation = routerMeta.correlation;
        event.put("correlation", logCorrelation);

        try {
            String key = MessageFormat.format(FORMAT_KEY_ROUTERS, routerMeta.envId, routerMeta.groupId, routerMeta.localIP);
            registerRouterAndUpdateTtl(routerMeta, event, key);
            final Set<Long> eTagRouters = processEtagsFromRouters(routerMeta);
            updateRouterMapCached(routerMeta, eTagRouters, logCorrelation);
        } catch (Exception e) {
            event.put("message",  "POST /routers/" + routerMeta.envId + " FAILED");
            event.sendError(e);
        }
    }

    private void registerRouterAndUpdateTtl(RouterMeta routerMeta, JsonEventToLogger event, String key) {
        if (!redisTemplate.hasKey(key)) {
            Long versionIncremented = versionService.incrementVersion(routerMeta.envId);

            event.put("keyAdded", key);
            event.put("versionIncremented", String.valueOf(versionIncremented));
            event.put("environmentId", routerMeta.envId);
            event.put("routerGroupId", routerMeta.groupId);
            event.put("routerVersion", routerMeta.version);
            event.put("zoneId", routerMeta.zoneId);
            event.put("message", "Registering router");
            event.sendInfo();
        }
        redisTemplate.opsForValue().set(key, routerMeta.version, REGISTER_TTL, TimeUnit.MILLISECONDS);
    }

    private void updateRouterMapCached(final RouterMeta routerMeta, final Set<Long> eTagRouters, String logCorrelation)
            throws ConverterNotFoundException {
        try {
            if (redisLock()) {
                deleteAllHasChangesProcessed(routerMeta, eTagRouters);
                rebuildRouterMapCached(routerMeta);
            }
        } finally {
            releaseRedisLock();
        }
    }

    private synchronized boolean redisLock() {
        boolean result = redisTemplate.opsForValue().setIfAbsent(ROUTER_MAP_CACHE_REBUILD_LOCK, "" + System.currentTimeMillis());
        redisTemplate.expire(ROUTER_MAP_CACHE_REBUILD_LOCK, ROUTER_CONCURRENT_LOCK_TTL, TimeUnit.MILLISECONDS);

        JsonEventToLogger event = new JsonEventToLogger(this.getClass());
        event.put("message", "Getting lock");
        event.sendInfo();

        return result;
    }

    private void releaseRedisLock() {
        if (redisTemplate.hasKey(ROUTER_MAP_CACHE_REBUILD_LOCK)) {
            JsonEventToLogger event = new JsonEventToLogger(this.getClass());
            event.put("message", "Releasing lock");
            event.sendInfo();
            redisTemplate.delete(ROUTER_MAP_CACHE_REBUILD_LOCK);
        }
    }

    private Set<Long> processEtagsFromRouters(RouterMeta routerMeta) {
        Set<Long> eTagRouters = new HashSet<>();
        String keyAll = MessageFormat.format(FORMAT_KEY_ROUTERS, routerMeta.envId, "*", "*");
        redisTemplate.keys(keyAll).forEach(routerKey -> {
            String logCorrelation = routerMeta.correlation;
            try {
                eTagRouters.add(Long.valueOf(redisTemplate.opsForValue().get(routerKey)));
            } catch (NumberFormatException e) {
                final JsonEventToLogger event = new JsonEventToLogger(this.getClass());
                event.put("correlation", logCorrelation);
                event.put("keyExpired", routerKey);
                event.put("environmentId", routerMeta.envId);
                event.put("message", "Version is not a number. Verify the environment " + routerMeta.envId);
                event.sendWarn();
            }
            expireIfNecessaryAndIncrementVersion(routerMeta, routerKey);
        });
        return eTagRouters;
    }

    private void rebuildRouterMapCached(RouterMeta routerMeta)
            throws ConverterNotFoundException {

        String cache = versionService.getCache(routerMeta.envId, routerMeta.zoneId);
        if (cache == null || "".equals(cache)) {
            long start = System.currentTimeMillis();

            // TODO: Add V2..Vx dynamic support
            final Converter converter = getConverter(DEFAULT_API_VERSION);
            int numRouters = get(routerMeta.envId, routerMeta.groupId);
            cache = converter.convertToString(routerMeta, numRouters);
            versionService.setCache(cache, routerMeta.envId, routerMeta.zoneId);

            JsonEventToLogger event = new JsonEventToLogger(this.getClass());
            event.put("message", "New Cache DONE");
            event.put("correlation", routerMeta.correlation);
            event.put("cacheSize", cache.length());
            event.put("cacheTimeMS", System.currentTimeMillis() - start);
            event.sendInfo();
        }
    }

    private void expireIfNecessaryAndIncrementVersion(RouterMeta routerMeta, String routerKey) {
        Long ttl = redisTemplate.getExpire(routerKey, TimeUnit.MILLISECONDS);
        if (ttl == null || ttl < (REGISTER_TTL/2)) {
            redisTemplate.delete(routerKey);
            // increment version to force num routers property rebuilding
            Long versionIncremented = versionService.incrementVersion(routerMeta.envId);
            final JsonEventToLogger event = new JsonEventToLogger(this.getClass());
            String logCorrelation = routerMeta.correlation;
            event.put("correlation", logCorrelation);
            event.put("keyExpired", routerKey);
            event.put("versionIncremented", String.valueOf(versionIncremented));
            event.put("environmentId", routerMeta.envId);
            event.put("message", "Update router state");
            event.sendInfo();
        }
    }

    private Converter getConverter(String apiVersion) throws ConverterNotFoundException {
        final Converter converter;
        if (apiVersion == null || ConverterV1.API_VERSION.equals(apiVersion)) {
            converter = converterV1;
        } else if (ConverterV2.API_VERSION.equals(apiVersion)) {
            converter = converterV2;
        } else {
            throw new ConverterNotFoundException();
        }
        return converter;
    }

    private void deleteAllHasChangesProcessed(RouterMeta routerMeta, final Set<Long> eTagRouters) {
        Long minVersionRouter = eTagRouters.stream().mapToLong(i -> i).min().orElse(-1L);
        String logCorrelation = routerMeta.correlation;
        changesService.listEntitiesWithOldestVersion(routerMeta.envId, minVersionRouter).stream()
            .filter(hasChangeData -> EntitiesRegistrable.contains(hasChangeData.entityClassName()))
            .forEach(hasChangeData -> deleteHasChangeAndEntityFromDB(routerMeta.envId, hasChangeData, logCorrelation));
    }

    private void deleteHasChangeAndEntityFromDB(String envId, HasChangeData<String, String, String> hasChangeData, String logCorrelation) {
        String entityClass = hasChangeData.entityClassName();
        String entityId = hasChangeData.entityId();
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        final EntityTransaction transaction = entityManager.getTransaction();
        final JsonEventToLogger event = new JsonEventToLogger(this.getClass());
        event.put("correlation", logCorrelation);
        event.put("entityId", entityId);
        event.put("entityClass", entityClass);
        event.put("environmentId", envId);
        try {
            transaction.begin();
            Query query = entityManager.createQuery("DELETE FROM " + entityClass + " e WHERE e.id = :entityId AND e.quarantine = true");
            query.setParameter("entityId", Long.parseLong(entityId));
            int numEntities = query.executeUpdate();
            transaction.commit();
            if (numEntities > 0) {
                event.put("message", "Delete HasChange and Entity from DB");
                event.sendInfo();
            }
            changesService.delete(hasChangeData.key());
        } catch (Exception e) {
            transaction.rollback();
            event.put("message", "Delete HasChange and Entity from DB FAILED");
            event.sendError(e);
        } finally {
            entityManager.close();
        }
    }

    @ResponseStatus(value= HttpStatus.BAD_REQUEST, reason = "Converter not found")
    public static class ConverterNotFoundException extends Exception {

    }
}
