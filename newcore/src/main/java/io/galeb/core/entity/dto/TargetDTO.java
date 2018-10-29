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

package io.galeb.core.entity.dto;

import io.galeb.core.entity.HealthStatus;
import io.galeb.core.entity.Pool;
import io.galeb.core.entity.Target;
import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class TargetDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // @formatter:off
    public static final String TARGET               = "target";
    public static final String POOL_NAME            = "pool";
    public static final String HC_PATH              = "hcPath";
    public static final String HC_HTTP_STATUS_CODE  = "hcHttpStatusCode";
    public static final String HC_HOST              = "hcHost";
    public static final String HC_TCP_ONLY          = "hcTcpOnly";
    public static final String HC_HTTP_METHOD       = "hcHttpMethod";
    public static final String HC_BODY              = "hcBody";
    public static final String HC_HEADERS           = "hcHeaders";
    public static final String HEALTH_STATUS        = "healthStatus";
    // @formatter:on

    private final Map<String, Object> properties = new HashMap<>();
    private final String correlation;

    public TargetDTO(Target target) {
        final Pool pool = target.getPool();
        final String hcHostDefault = hcHostDefaultFromTarget(target);
        properties.put(TARGET, target);
        properties.put(POOL_NAME, pool.getName());
        properties.put(HC_PATH, Optional.ofNullable(pool.getHcPath()).orElse("/"));
        properties.put(HC_HTTP_STATUS_CODE, Optional.ofNullable(pool.getHcHttpStatusCode()).orElse(""));
        properties.put(HC_HOST, Optional.ofNullable(pool.getHcHost()).orElse(hcHostDefault));
        properties.put(HC_TCP_ONLY, pool.getHcTcpOnly());
        properties.put(HC_HTTP_METHOD, pool.getHcHttpMethod().toString());
        properties.put(HC_BODY, Optional.ofNullable(pool.getHcBody()).orElse(""));
        properties.put(HC_HEADERS, pool.getHcHeaders());
        properties.put(HEALTH_STATUS, target.getHealthStatus());
        this.correlation = UUID.randomUUID().toString();
    }

    private String hcHostDefaultFromTarget(Target target) {
        URI targetURI;
        try {
            String targetName = target.getName();
            targetURI = URI.create(targetName);
        } catch (Exception ignore) {
            targetURI = URI.create("http://invalidhost.invaliddomain");
        }
        int port = targetURI.getPort();
        if (port <= 0) {
            if ("https".equalsIgnoreCase(targetURI.getScheme())) {
                port = 443;
            } else {
                port = 80;
            }
        }
        return targetURI.getHost() + ":" + port;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public boolean equals(Object o) {
        return properties.equals(o);
    }

    @Override
    public int hashCode() {
        return properties.hashCode();
    }

    public String getCorrelation() {
        return this.correlation;
    }

    public Target getTarget() {
        return (Target) properties.get(TARGET);
    }

    @SuppressWarnings("unchecked")
    public Optional<HealthStatus> getHealthStatus(String source) {
        return ((Set<HealthStatus>)properties.get(HEALTH_STATUS)).stream()
            .filter(h -> h.getSource().equals(source)).findAny();
    }
}
