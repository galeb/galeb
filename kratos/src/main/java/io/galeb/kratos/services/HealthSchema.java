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
import java.util.Objects;
import java.util.Set;

public class HealthSchema {

    public static class Env implements Comparable<Env> {

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

        @Override
        public int compareTo(Env other) {
            return other == null ? -1 : this.envId.compareTo(other.getEnvId());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Env env = (Env) o;
            return Objects.equals(envId, env.envId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(envId);
        }
    }

    public static class Source implements Comparable<Source> {
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

        @Override
        public int compareTo(Source other) {
            return other == null ? -1 : this.name.compareTo(other.getName());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Source source = (Source) o;
            return Objects.equals(name, source.getName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    public static class Health implements Comparable<Health> {
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

        @Override
        public int compareTo(Health other) {
            return other == null ? -1 : this.localIps.compareTo(other.getLocalIps());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Health health = (Health) o;
            return Objects.equals(localIps, health.getLocalIps());
        }

        @Override
        public int hashCode() {
            return Objects.hash(localIps);
        }
    }
}
