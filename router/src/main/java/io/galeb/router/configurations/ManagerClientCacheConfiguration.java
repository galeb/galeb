/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
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

package io.galeb.router.configurations;

import io.galeb.core.entity.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

@Configuration
public class ManagerClientCacheConfiguration {

    public static final String FULLHASH_PROP = "fullhash";

    @Bean
    ManagerClientCache managerClientCache() {
        return new ManagerClientCache();
    }

    public static class ManagerClientCache {

        public static final String EMPTY = "EMPTY";

        private final ConcurrentHashMap<String, VirtualHost> virtualHosts = new ConcurrentHashMap<>();

        private String envHash = null;

        public VirtualHost get(String hostName) {
            return virtualHosts.get(hostName);
        }

        public synchronized void put(String virtualhostName, final VirtualHost virtualHost) {
            envHash = virtualHost.getEnvironment().getProperties().get(FULLHASH_PROP);
            virtualHosts.put(virtualhostName, virtualHost);
        }

        public boolean isEmpty() {
            return virtualHosts.isEmpty();
        }

        public boolean exist(String virtualhostName) {
            return virtualHosts.containsKey(virtualhostName);
        }

        public synchronized void remove(String virtualhostName) {
            virtualHosts.remove(virtualhostName);
        }

        public Set<String> getAll() {
            return virtualHosts.keySet();
        }

        public MapDifference<String, VirtualHost> diff(Map<String, VirtualHost> newMap) {
            return Maps.difference(virtualHosts, newMap);
        }

        public synchronized String etag() {
            return envHash == null ? EMPTY : envHash;
        }

        public synchronized void updateEtag(String newHash) {
            Assert.notNull(newHash, "Update Etag not possible: new Hash IS NULL");
            if (!newHash.equals(this.envHash)) this.envHash = newHash;
        }

        public List<VirtualHost> values() {
            return new ArrayList<>(virtualHosts.values());
        }

        public synchronized void clear() {
            envHash = null;
            virtualHosts.clear();
        }
    }
}
