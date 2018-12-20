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


import io.galeb.core.log.JsonEventToLogger;
import java.util.Comparator;
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

    @Autowired
    StringRedisTemplate redisTemplate;

    public String getActualVersion(String envId) {
        String version = redisTemplate.opsForValue().get(MessageFormat.format(FORMAT_KEY_VERSION, envId));
        if (version == null) {
            version = String.valueOf(redisTemplate.opsForValue().increment(MessageFormat.format(FORMAT_KEY_VERSION, envId), 1));
        }
        return version;
    }

    public String getCache(String envId, String zoneId, String actualVersion) {
        return redisTemplate.opsForValue().get(MessageFormat.format(FORMAT_KEY_CACHE, envId, zoneId, lastCacheVersion(envId, zoneId, actualVersion)));
    }

    public void setCache(String cache, String envId, String zoneId, String actualVersion) {
        redisTemplate.opsForValue().set(MessageFormat.format(FORMAT_KEY_CACHE, envId, zoneId, actualVersion), cache, 5, TimeUnit.MINUTES);
    }

    public Long incrementVersion(String envId) {
        String keyFormatted = MessageFormat.format(FORMAT_KEY_VERSION, envId);
        return redisTemplate.opsForValue().increment(keyFormatted, 1);
    }

    public String lastCacheVersion(String envId, String zoneId, String actualVersion) {
        String keyCache = MessageFormat.format(FORMAT_KEY_CACHE, envId, zoneId == null ? "*" : zoneId, "*");
        String lastKeyCache = redisTemplate.keys(keyCache).stream().sorted(Comparator.reverseOrder()).limit(1).findAny()
            .orElse(MessageFormat.format(FORMAT_KEY_CACHE, envId, zoneId, "0"));
        String[] lastCacheVersionArray = lastKeyCache.split(":");
        String lastCacheVersion = lastCacheVersionArray.length > 3 ? lastCacheVersionArray[3] : "0";
        if (!lastCacheVersion.equals(actualVersion)) {
            JsonEventToLogger event = new JsonEventToLogger(this.getClass());
            event.put("message", "lastCacheVersion != actualVersion");
            event.put("lastCacheVersion", lastCacheVersion);
            event.put("actualVersion", actualVersion);
            event.sendInfo();
        }
        return lastCacheVersion;
    }

}

