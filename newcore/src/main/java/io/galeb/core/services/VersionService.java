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


import io.galeb.core.enums.SystemEnv;
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
     * {1} - Zone Id
     * {1} - Version number
     */
    private static final String FORMAT_KEY_CACHE = "cache:{0}:{1}:{2}";

    private static final String KEY_LAST_VERSION = "lastversion";

    private static final long ROUTER_MAP_VERSION_TTL = Long.parseLong(SystemEnv.ROUTER_MAP_VERSION_TTL.getValue());

    private final Object incrementLock = new Object();

    @Autowired
    StringRedisTemplate redisTemplate;

    public String getActualVersion(String envId) {
        String version = redisTemplate.opsForValue().get(MessageFormat.format(FORMAT_KEY_VERSION, envId));
        String lastVersion = redisTemplate.opsForValue().get(KEY_LAST_VERSION);
        boolean versionExpired = lastVersion != null && System.currentTimeMillis() - Long.parseLong(lastVersion) > ROUTER_MAP_VERSION_TTL;
        if (version == null || versionExpired) {
            version = String.valueOf(redisTemplate.opsForValue().increment(MessageFormat.format(FORMAT_KEY_VERSION, envId), 1));
        }
        return version;
    }

    public String getCache(String envId, String zoneId, String actualVersion) {
        return redisTemplate.opsForValue().get(MessageFormat.format(FORMAT_KEY_CACHE, envId, zoneId, actualVersion));
    }

    public void setCache(String cache, String envId, String zoneId, String actualVersion) {
        redisTemplate.opsForValue().set(MessageFormat.format(FORMAT_KEY_CACHE, envId, zoneId, actualVersion), cache, 5, TimeUnit.MINUTES);
    }

    public Long incrementVersion(String envId) {
        Long newVersion;
        String keyFormatted = MessageFormat.format(FORMAT_KEY_VERSION, envId);
        synchronized (incrementLock) {
            redisTemplate.opsForValue().set(KEY_LAST_VERSION, Long.toString(System.currentTimeMillis()));
            newVersion = redisTemplate.opsForValue().increment(keyFormatted, 1);
        }
        return newVersion;
    }

}

