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

package io.galeb.kratos.services;

import io.galeb.core.log.JsonEventToLogger;
import io.galeb.kratos.services.HealthSchema.Health;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class HealthService {

    /**
     * Description arguments:
     *  {0} - Environment Id
     *  {1} - Source Name
     *  {2} - LocalIps
     */
    private static final String FORMAT_KEY_HEALTH = "health:{0}:{1}:{2}";

    public static long REGISTER_TTL = Long
        .valueOf(Optional.ofNullable(System.getenv("REGISTER_HEALTH_TTL")).orElse("30000")); // ms

    @Autowired
    StringRedisTemplate redisTemplate;

    public Set<HealthSchema.Env> get() {
        return get(null);
    }

    public Set<HealthSchema.Env> get(String environmentId) {
        try {
            Assert.notNull(redisTemplate, StringRedisTemplate.class.getSimpleName() + " IS NULL");

            final Set<HealthSchema.Env> envs = new HashSet<>();
            String keyEnvId = environmentId == null ? "*" : environmentId;
            redisTemplate.keys(MessageFormat.format(FORMAT_KEY_HEALTH, keyEnvId, "*", "*")).forEach(key -> {
                try {
                    long expire = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
                    MessageFormat ms = new MessageFormat(FORMAT_KEY_HEALTH);
                    Object[] positions = ms.parse(key);
                    String envId = (String) positions[0];
                    String healthGroup = (String) positions[1];
                    String localIps = (String) positions[2];

                    envs.add(new HealthSchema.Env(envId, new HashSet<>()));
                    HealthSchema.Env envSchema = envs.stream()
                        .filter(e -> e.getEnvId().equals(envId))
                        .findAny()
                        .get();

                    Set<HealthSchema.Source> sources = envSchema.getSources();
                    sources.add(new HealthSchema.Source(healthGroup, new HashSet<>()));
                    HealthSchema.Source sourceSchema = sources.stream()
                        .filter(s -> s.getName().equals(healthGroup))
                        .findAny()
                        .get();

                    final Health health = new Health(localIps, expire);
                    sourceSchema.getHealths().remove(health);
                    sourceSchema.getHealths().add(health);

                } catch (Exception e) {
                    JsonEventToLogger errorEvent = new JsonEventToLogger(this.getClass());
                    errorEvent.sendError(e);
                }
            });
            return envs;
        } catch (Exception e) {
            JsonEventToLogger errorEvent = new JsonEventToLogger(this.getClass());
            errorEvent.sendError(e);
        }
        return Collections.emptySet();
    }

    public void put(String message) {
        try {
            Assert.notNull(redisTemplate, StringRedisTemplate.class.getSimpleName() + " IS NULL");
            redisTemplate.opsForValue()
                .set(message, String.valueOf(System.currentTimeMillis()), REGISTER_TTL, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            JsonEventToLogger errorEvent = new JsonEventToLogger(this.getClass());
            errorEvent.sendError(e);
        }
    }

}