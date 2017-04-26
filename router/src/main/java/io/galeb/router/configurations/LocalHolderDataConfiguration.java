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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Configuration
public class LocalHolderDataConfiguration {

    @Bean
    LocalHolderData localHolderData() {
        return new LocalHolderData();
    }

    public static class LocalHolderData {
        private final ConcurrentHashMap<String, VirtualHost> virtualHosts = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<Long, Pool> pools = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<Long, Target> targets = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<Long, Rule> rules = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<Long, RuleType> ruleTypes = new ConcurrentHashMap<>();
        private final AtomicLong lastUpdate = new AtomicLong(0L);

        public synchronized Long lastUpdate() {
            return lastUpdate.get();
        }

        public synchronized VirtualHost virtualHostByName(String hostName) {
            return virtualHosts.get(hostName);
        }

        public synchronized void putVirtualhost(String virtualhostName, final VirtualHost virtualHost) {
            virtualHosts.put(virtualhostName, virtualHost);
            lastUpdate.set(System.currentTimeMillis());
        }

        public synchronized void removeVirtualhost(String virtualhostName) {
            virtualHosts.remove(virtualhostName);
            lastUpdate.set(System.currentTimeMillis());
        }

        public synchronized boolean isVirtualhostsEmpty() {
            return virtualHosts.isEmpty();
        }

        public synchronized Set<VirtualHost> getAllVirtualhost() {
            return new HashSet<>(virtualHosts.values());
        }

        public synchronized boolean virtualHostExist(String virtualhostName) {
            return virtualHosts.containsKey(virtualhostName);
        }

        public synchronized Set<Rule> getRulesByVirtualhost(VirtualHost virtualHost) {
            return rules.values().stream().filter(rule -> rule.getParents().stream()
                    .anyMatch(v -> v.getName().equals(virtualHost.getName()))).collect(Collectors.toSet());
        }

        public synchronized BalancePolicy getBalancePolicyByPool(Pool pool) {
            return null;
        }

        public synchronized List<Target> getTargetsByPool(Pool pool) {
            return null;
        }

        public synchronized Pool getPoolById(Long poolId) {
            return null;
        }

        public void removePool(Long poolId) {
            pools.remove(poolId);
            lastUpdate.set(System.currentTimeMillis());
        }

        public void putPool(Long poolId, Pool pool) {
            pools.put(poolId, pool);
            lastUpdate.set(System.currentTimeMillis());
        }

        public void removeRule(long id) {
            rules.remove(id);
            lastUpdate.set(System.currentTimeMillis());
        }

        public void putRule(long id, Rule rule) {
            rules.put(id, rule);
            lastUpdate.set(System.currentTimeMillis());
        }

        public void removeTarget(long id) {
            targets.remove(id);
            lastUpdate.set(System.currentTimeMillis());
        }

        public void putTarget(long id, Target target) {
            targets.put(id, target);
            lastUpdate.set(System.currentTimeMillis());
        }
    }
}
