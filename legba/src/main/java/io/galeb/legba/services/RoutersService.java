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
import io.galeb.core.log.JsonEventToLogger;
import io.galeb.core.services.ChangesService;
import io.galeb.core.services.VersionService;
import io.galeb.legba.common.ErrorLogger;
import io.galeb.legba.conversors.Converter;
import io.galeb.legba.conversors.ConverterV1;
import io.galeb.legba.conversors.ConverterV2;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ResponseStatus;

@Service
public class RoutersService {

    private static final Log LOGGER = LogFactory.getLog(RoutersService.class);

    /**
     * Description arguments:
     * {0} - Environment Id
     * {1} - Group ID
     * {2} - Local IP
     */
    private static final String FORMAT_KEY_ROUTERS = "routers:{0}:{1}:{2}";

    public static long REGISTER_TTL  = Long.valueOf(Optional.ofNullable(System.getenv("REGISTER_ROUTER_TTL")).orElse("30000")); // ms

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ChangesService changesService;

    @Autowired
    private VersionService versionService;

    @Autowired
    private ConverterV1 converterV1;

    @Autowired
    private ConverterV2 converterV2;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    public Set<JsonSchema.Env> get() {
        return get(null);
    }

    public Set<JsonSchema.Env> get(String environmentId) {
        try {
            Assert.notNull(redisTemplate, StringRedisTemplate.class.getSimpleName() + " IS NULL");

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
            ErrorLogger.logError(e, this.getClass());
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

    public void put(String routerGroupId, String routerLocalIP, String routerVersion, String envId, String zoneId) {
        final JsonEventToLogger event = new JsonEventToLogger(this.getClass());
        String logCorrelation = UUID.randomUUID().toString();

        try {
            String key = MessageFormat.format(FORMAT_KEY_ROUTERS, envId, routerGroupId, routerLocalIP);
            Assert.notNull(redisTemplate, StringRedisTemplate.class.getSimpleName() + " IS NULL");
            if (!redisTemplate.hasKey(key)) {
                Long versionIncremented = versionService.incrementVersion(envId);

                event.put("correlation", logCorrelation);
                event.put("keyAdded", key);
                event.put("versionIncremented", String.valueOf(versionIncremented));
                event.put("environmentId", envId);
                event.put("routerGroupId", routerGroupId);
                event.put("routerVersion", routerVersion);
                event.put("zoneId", zoneId);
                event.put("message", "Registering router");
                event.sendInfo();
            }
            redisTemplate.opsForValue().set(key, routerVersion, REGISTER_TTL, TimeUnit.MILLISECONDS);
            updateRouterState(envId, "v2", routerVersion, zoneId == null ? "" : zoneId, routerGroupId, logCorrelation);
        } catch (Exception e) {
            ErrorLogger.logError(e, this.getClass());
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void updateRouterState(
            String envId,
            String apiVersion,
            String routerVersion,
            String zoneId,
            String groupId,
            String logCorrelation)

        throws ConverterNotFoundException {
        Assert.notNull(redisTemplate, StringRedisTemplate.class.getSimpleName() + " IS NULL");
        Set<Long> eTagRouters = new HashSet<>();
        String keyAll = MessageFormat.format(FORMAT_KEY_ROUTERS, envId, "*", "*");
        redisTemplate.keys(keyAll).forEach(key -> {
            try {
                eTagRouters.add(Long.valueOf(redisTemplate.opsForValue().get(key)));
            } catch (NumberFormatException e) {
                LOGGER.warn("Version is not a number. Verify the environment " + envId);
            }
            Long ttl = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
            if (ttl == null || ttl < (REGISTER_TTL/2)) {
                redisTemplate.delete(key);
                Long versionIncremented = versionService.incrementVersion(envId);
                final JsonEventToLogger event = new JsonEventToLogger(this.getClass());
                event.put("correlation", logCorrelation);
                event.put("keyExpired", key);
                event.put("versionIncremented", String.valueOf(versionIncremented));
                event.put("environmentId", envId);
                event.put("message", "Update router state");
                event.sendInfo();
            }
        });
        Long versionRouter = eTagRouters.stream().mapToLong(i -> i).min().orElse(-1L);

        changesService.listEntitiesWithOldestVersion(envId, versionRouter).stream()
                .filter(hasChangeData -> EntitiesRegistrable.contains(hasChangeData.entityClassName()))
                .forEach(hasChangeData -> deleteHasChangeAndEntityFromDB(envId, hasChangeData, logCorrelation));

        final Converter converter;
        if (apiVersion == null || ConverterV1.API_VERSION.equals(apiVersion)) {
            converter = converterV1;
        } else if (ConverterV2.API_VERSION.equals(apiVersion)) {
            converter = converterV2;
        } else {
            throw new ConverterNotFoundException();
        }

        String cache = versionService.getCache(envId, zoneId, routerVersion);
        if (cache == null || "".equals(cache)) {
            cache = converter.convertToString(logCorrelation, routerVersion, zoneId, Long.getLong(envId), groupId);
            versionService.setCache(cache, envId, zoneId, routerVersion);
        }
    }

    private void deleteHasChangeAndEntityFromDB(String envId, HasChangeData<String, String, String> hasChangeData, String logCorrelation) {
        String entityClass = hasChangeData.entityClassName();
        String entityId = hasChangeData.entityId();
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        final EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            Query query = entityManager.createQuery("DELETE FROM " + entityClass + " e WHERE e.id = :entityId AND e.quarantine = true");
            query.setParameter("entityId", Long.parseLong(entityId));
            int numEntities = query.executeUpdate();
            transaction.commit();
            if (numEntities > 0) {
                final JsonEventToLogger event = new JsonEventToLogger(this.getClass());
                event.put("correlation", logCorrelation);
                event.put("entityIdDeleted", entityId);
                event.put("entityClassDeleted", entityClass);
                event.put("environmentId", envId);
                event.put("message", "Delete HasChange and Entity from DB");
                event.sendInfo();
            }
            changesService.delete(hasChangeData.key());
        } catch (Exception e) {
            transaction.rollback();
            LOGGER.error(e.getMessage(), e);
        } finally {
            entityManager.close();
        }
    }

    @ResponseStatus(value= HttpStatus.BAD_REQUEST, reason = "Converter not found")
    public static class ConverterNotFoundException extends Exception {

    }
}
