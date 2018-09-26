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

import java.util.HashSet;
import java.util.Set;

public class HealthSchema {

    public static class Env {

        private final String envId;
        private final Set<Source> sources;

        public Env(String envId, Set<Source> sources) {
            this.envId = envId;
            this.sources = sources;
        }

        public String getEnvId() {
            return envId;
        }

        public Set<Source> getSources() {
            return sources;
        }
    }

    public static class Source {
        private String name;
        private Set<Health> healths = new HashSet<>();

        public Source(String name, Set<Health> healths) {
            this.name = name;
            this.healths.addAll(healths);
        }

        public String getName() {
            return name;
        }

        public Set<Health> getHealths() {
            return healths;
        }
    }

    public static class Health {
        private final String localIps;
        private final long expire;

        public Health(String localIps, long expire) {
            this.localIps = localIps;
            this.expire = expire;
        }

        public String getLocalIps() {
            return localIps;
        }

        public long getExpire() {
            return expire;
        }
    }
}
