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

package io.galeb.legba.conversors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.galeb.core.entity.HealthStatus;
import io.galeb.core.entity.HealthStatus.Status;
import io.galeb.core.entity.Target;
import io.galeb.core.entity.VirtualHost;

import io.galeb.core.log.JsonEventToLogger;
import io.galeb.legba.model.v2.Pool;
import io.galeb.legba.model.v2.Rule;
import io.galeb.legba.model.v2.RuleOrdered;
import io.galeb.legba.model.v2.VirtualhostGroup;
import io.galeb.legba.repository.VirtualHostRepository;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConverterV2 implements Converter {

    public static final String API_VERSION = "v2";

    private final Gson gson = new GsonBuilder().serializeNulls().create();

    @Autowired
    private VirtualHostRepository virtualHostRepository;

    @Override
    public String convertToString(String logCorrelation, String version, String zoneId, Long envId, String groupId, int numRouters) {
        List<io.galeb.legba.model.v2.VirtualHost> list = new ArrayList<>();
        final List<VirtualHost> virtualHostsV2 = virtualHostRepository.findAllByEnvironmentId(envId);
        JsonEventToLogger event = new JsonEventToLogger(this.getClass());
        event.put("message", "Converting to string");
        event.put("numRouters", numRouters);
        event.put("numVirtualHost", String.valueOf(virtualHostsV2.size()));
        event.put("correlation", logCorrelation);
        event.sendInfo();
        virtualHostsV2.forEach(vh -> {
            io.galeb.legba.model.v2.VirtualHost v = new io.galeb.legba.model.v2.VirtualHost();
            v.setName(vh.getName());
            v.setVersion(version);
            v.setVirtualhostGroup(convertVirtualhostGroup(vh.getVirtualhostgroup(), numRouters, zoneId, groupId, envId));
            list.add(v);

        });
        Map<String, List<io.galeb.legba.model.v2.VirtualHost>> newList = new HashMap<>();
        newList.put("virtualhosts", list);
        return gson.toJson(newList);
    }

    private VirtualhostGroup convertVirtualhostGroup(io.galeb.core.entity.VirtualhostGroup virtualhostgroup, int numRouters, String zoneId, String groupId, Long envId) {

        VirtualhostGroup vhg = new VirtualhostGroup();
        Set<RuleOrdered> ruleOrdereds = new HashSet<>();
        virtualhostgroup.getRulesordered().forEach(ro -> {
            RuleOrdered roLocal = new RuleOrdered();
            roLocal.setOrder(ro.getOrder());

            Rule rule = new Rule();
            rule.setGlobal(ro.getRule().getGlobal());
            rule.setMatching(ro.getRule().getMatching());
            rule.setName(ro.getRule().getMatching());
            rule.setPools(convertPools(ro.getRule().getPools(), numRouters, zoneId, groupId, envId));

            roLocal.setRule(rule);
            ruleOrdereds.add(roLocal);
        });

        vhg.setRulesordered(ruleOrdereds);
        return vhg;
    }

    private Set<Pool> convertPools(Set<io.galeb.core.entity.Pool> pools, int numRouters, String zoneId, String groupId, Long envId) {
        Set<Pool> poolsLocal = new HashSet<>();
        pools.forEach(p -> {
            Pool pool = new Pool();
            pool.setName(p.getName());
            pool.setDiscoveredMembersSize(String.valueOf(numRouters));

            Set<io.galeb.legba.model.v2.Target> targets = new HashSet<>();
            p.getTargets().forEach(t -> {
                Set<HealthStatus> healthStatuses = t.getHealthStatus();
                if (healthStatuses == null || healthStatuses.isEmpty()) {
                    targets.add(newTargetV2(t));
                } else {
                    healthStatuses.stream()
                        .filter(hs ->
                            (zoneId == null || hs.getSource().equals(zoneId)) &&
                            !hs.getStatus().equals(Status.FAIL))
                        .forEach(hs -> targets.add(newTargetV2(t)));
                }
            });
            pool.setTargets(targets);
            poolsLocal.add(pool);
        });
        return poolsLocal;
    }

    private io.galeb.legba.model.v2.Target newTargetV2(Target target) {
        io.galeb.legba.model.v2.Target targetV2 = new io.galeb.legba.model.v2.Target();
        target.setName(target.getName());
        return targetV2;
    }

    @Override
    public String getApiVersion() {
        return API_VERSION;
    }
}
