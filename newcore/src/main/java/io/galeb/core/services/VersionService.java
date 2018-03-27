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

    public String getActualVersion(String envid) {
        String version = redisTemplate.opsForValue().get(MessageFormat.format(FORMAT_KEY_VERSION, envid));
        if (version == null) {
            version = String.valueOf(redisTemplate.opsForValue().increment(MessageFormat.format(FORMAT_KEY_VERSION, envid), 1));
        }
        return version;
    }

    public String getCache(String envid, String zoneId, String version) {
        return redisTemplate.opsForValue().get(MessageFormat.format(FORMAT_KEY_CACHE, envid, zoneId, version));
    }

    public void setCache(String cache, String envid, String zoneId, String version) {
        redisTemplate.opsForValue().set(MessageFormat.format(FORMAT_KEY_CACHE, envid, zoneId, version), cache, 5, TimeUnit.MINUTES);
    }

    public Long incrementVersion(Object envId) {
        String keyFormatted = MessageFormat.format(FORMAT_KEY_VERSION, envId);
        return redisTemplate.opsForValue().increment(keyFormatted, 1);
    }

}

