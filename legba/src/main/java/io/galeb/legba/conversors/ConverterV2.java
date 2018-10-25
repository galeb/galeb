package io.galeb.legba.conversors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.galeb.core.entity.HealthStatus;
import io.galeb.core.entity.HealthStatus.Status;
import io.galeb.core.entity.Target;
import io.galeb.core.entity.VirtualHost;

import io.galeb.legba.model.v2.Pool;
import io.galeb.legba.model.v2.Rule;
import io.galeb.legba.model.v2.RuleOrdered;
import io.galeb.legba.model.v2.VirtualhostGroup;
import java.util.*;

public class ConverterV2 implements Converter {

    public static final String API_VERSION = "v2";

    private final Gson gson = new GsonBuilder().serializeNulls().create();

    @Override
    public String convertToString(List<VirtualHost> virtualHostList, String numRouters, String version, String networkId, Long envId) {
        List<io.galeb.legba.model.v2.VirtualHost> list = new ArrayList<>();
        virtualHostList.forEach(vh -> {

            io.galeb.legba.model.v2.VirtualHost v = new io.galeb.legba.model.v2.VirtualHost();
            v.setName(vh.getName());
            v.setVersion(version);
            v.setVirtualhostGroup(convertVirtualhostGroup(vh.getVirtualhostgroup(), numRouters, networkId));
            list.add(v);

        });
        Map<String, List<io.galeb.legba.model.v2.VirtualHost>> newList = new HashMap<>();
        newList.put("virtualhosts", list);
        return gson.toJson(newList);
    }

    private VirtualhostGroup convertVirtualhostGroup(io.galeb.core.entity.VirtualhostGroup virtualhostgroup, String numRouters, String networkId) {

        VirtualhostGroup vhg = new VirtualhostGroup();
        Set<RuleOrdered> ruleOrdereds = new HashSet<>();
        virtualhostgroup.getRulesordered().forEach(ro -> {
            RuleOrdered roLocal = new RuleOrdered();
            roLocal.setOrder(ro.getOrder());

            Rule rule = new Rule();
            rule.setGlobal(ro.getRule().getGlobal());
            rule.setMatching(ro.getRule().getMatching());
            rule.setName(ro.getRule().getMatching());
            rule.setPools(convertPools(ro.getRule().getPools(), numRouters, networkId));

            roLocal.setRule(rule);
            ruleOrdereds.add(roLocal);
        });

        vhg.setRulesordered(ruleOrdereds);
        return vhg;
    }

    private Set<Pool> convertPools(Set<io.galeb.core.entity.Pool> pools, String numRouters, String networkId) {
        Set<Pool> poolsLocal = new HashSet<>();
        pools.forEach(p -> {
            Pool pool = new Pool();
            pool.setName(p.getName());
            pool.setDiscoveredMembersSize(numRouters);

            Set<io.galeb.legba.model.v2.Target> targets = new HashSet<>();
            p.getTargets().forEach(t -> {
                Set<HealthStatus> healthStatuses = t.getHealthStatus();
                if (healthStatuses == null || healthStatuses.isEmpty()) {
                    targets.add(newTargetV2(t));
                } else {
                    healthStatuses.stream()
                        .filter(hs ->
                            (networkId == null || hs.getSource().equals(networkId)) &&
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
}
