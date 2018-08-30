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

package io.galeb.core.services;

import io.galeb.core.entity.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChangesService {

    public static final List<String> entitiesRegistrable = Arrays.asList(Target.class.getSimpleName(),
                                                                         Pool.class.getSimpleName(),
                                                                         VirtualhostGroup.class.getSimpleName(),
                                                                         VirtualHost.class.getSimpleName(),
                                                                         RuleOrdered.class.getSimpleName(),
                                                                         Rule.class.getSimpleName());

    /**
     * Description arguments:
     * {0} - Environment Id
     * {1} - Entity Simple Name Class
     * {2} - Entity Id
     * {3} - Entity Last Modified At
     */
    private static final String FORMAT_KEY_HAS_CHANGE = "haschange:{0}:{1}:{2}:{3}";

    private static final Log LOGGER = LogFactory.getLog(ChangesService.class);

    @Autowired
    StringRedisTemplate redisTemplate;

    public void register(Environment e, AbstractEntity entity, String version) {
        if (!entitiesRegistrable.contains(entity.getClass().getSimpleName())) {
            return;
        }
        String simpleNameClass = entity.getClass().getSimpleName();
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
        String simpleNameClass = entity.getClass().getSimpleName();
        Long entityId = entity.getId();
        String keyFormatted = MessageFormat.format(FORMAT_KEY_HAS_CHANGE, "*",simpleNameClass, entityId, "*");
        Set<String> keys = keys(keyFormatted);
        return keys.stream().map(k -> Long.parseLong(new MessageFormat(FORMAT_KEY_HAS_CHANGE).parse(k, new ParsePosition(0))[0].toString())).collect(Collectors.toSet());
    }

    public Map<String, Map<String, String>> listEntitiesWithOldestVersion(String envid, Long version) {
        Map<String, Map<String, String>> entities = new HashMap<>();
        String key = MessageFormat.format(FORMAT_KEY_HAS_CHANGE, envid, "*", "*", "*");
        keys(key).stream().forEach(k -> {
            String value = redisTemplate.opsForValue().get(k);
            if (version >= Long.valueOf(value)) {
                String entityId = null;
                try {
                    entityId = (String) new MessageFormat(FORMAT_KEY_HAS_CHANGE).parse(k)[2];
                    String entityClass = (String) new MessageFormat(FORMAT_KEY_HAS_CHANGE).parse(k)[1];
                    Map map = new HashMap<>();
                    map.put(entityClass, entityId);
                    entities.put(k, map);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        return entities;
    }

    @Deprecated
    public void removeAllWithOldestVersion(String envid, Long version) {
        String key = MessageFormat.format(FORMAT_KEY_HAS_CHANGE, envid, "*", "*", "*");
        keys(key).stream().forEach(k -> {
            String value = redisTemplate.opsForValue().get(k);
            if (version >= Long.valueOf(value)) {
                redisTemplate.delete(k);
            }
        });
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    private boolean hasKey(String key) {
        final Set<String> result = keys(key);
        return result != null && !result.isEmpty();
    }

    private Set<String> keys(String key) {
        return redisTemplate.keys(key);
    }

}
