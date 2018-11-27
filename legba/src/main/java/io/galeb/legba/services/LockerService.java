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

    private static final String ROUTER_MAP_CACHE_REBUILD_LOCK = "router-map-cache-rebuild-lock";

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public LockerService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setExpireLock() {
        redisTemplate.expire(ROUTER_MAP_CACHE_REBUILD_LOCK, ROUTER_CONCURRENT_LOCK_TTL, TimeUnit.MILLISECONDS);
    }

    public synchronized boolean lock() {
        return redisTemplate.opsForValue().setIfAbsent(ROUTER_MAP_CACHE_REBUILD_LOCK, "" + System.currentTimeMillis());
    }

    public void release() {
        if (redisTemplate.hasKey(ROUTER_MAP_CACHE_REBUILD_LOCK)) {
            JsonEventToLogger event = new JsonEventToLogger(this.getClass());
            event.put("message", "Releasing lock");
            event.sendInfo();
            redisTemplate.delete(ROUTER_MAP_CACHE_REBUILD_LOCK);
        }
    }
}
