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

import io.galeb.core.log.JsonEventToLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class LockerService {

    private static long ROUTER_CONCURRENT_LOCK_TTL = 120000L; // Long.parseLong(SystemEnv.ROUTER_CONCURRENT_LOCK_TTL.getValue())

    private static final String ROUTER_MAP_CACHE_REBUILD_LOCK = "lock-router-map-cache-rebuild";

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public LockerService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public synchronized boolean notLocked(String zoneId) {
        if (redisTemplate.hasKey(ROUTER_MAP_CACHE_REBUILD_LOCK + "-" + zoneId)) {
            return false;
        } else {
            redisTemplate.opsForValue().set(ROUTER_MAP_CACHE_REBUILD_LOCK + "-" + zoneId, "" + System.currentTimeMillis(), ROUTER_CONCURRENT_LOCK_TTL, TimeUnit.MILLISECONDS);
            return true;
        }
    }

    public void release(String zoneId, String correlation) {
        if (redisTemplate.hasKey(ROUTER_MAP_CACHE_REBUILD_LOCK + "-" + zoneId)) {
            JsonEventToLogger event = new JsonEventToLogger(this.getClass());
            event.put("short_message", "Releasing lock");
            event.put("correlation", correlation);
            event.sendInfo();
            redisTemplate.delete(ROUTER_MAP_CACHE_REBUILD_LOCK + "-" + zoneId);
        }
    }
}
