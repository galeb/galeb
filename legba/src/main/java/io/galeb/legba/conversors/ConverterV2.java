package io.galeb.legba.conversors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.galeb.core.entity.HealthStatus;
import io.galeb.core.entity.VirtualHost;
import io.galeb.legba.model.v2.*;

import java.util.*;
import java.util.stream.Collectors;

public class ConverterV2 implements Converter {

    public static final String API_VERSION = "v2";

    private final Gson gson = new GsonBuilder().serializeNulls().create();

    @Override
    public String convertToString(List<VirtualHost> virtualHostList, String numRouters, String version, String networkId, Long envId) {
        List<io.galeb.legba.model.v2.VirtualHost> list = new ArrayList<>();
        virtualHostList.stream().forEach(vh -> {

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
        virtualhostgroup.getRulesordered().stream().forEach(ro -> {
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
        pools.stream().forEach(p -> {
            Pool pool = new Pool();
            pool.setName(p.getName());
            pool.setDiscoveredMembersSize(numRouters);

            Set<Target> targets = new HashSet<>();
            p.getTargets().stream().forEach(t -> {
                Set<HealthStatus> healthStatusesOK = t.getHealthStatus()
                        .stream()
                        .filter(hs -> (networkId == null || hs.getSource().equals(networkId)) && hs.getStatus().equals(HealthStatus.Status.HEALTHY))
                        .collect(Collectors.toSet());
                healthStatusesOK.stream().forEach(hs -> {
                    Target target = new Target();
                    target.setName(hs.getTarget().getName());
                    targets.add(target);
                });
            });
            pool.setTargets(targets);
            poolsLocal.add(pool);
        });
        return poolsLocal;
    }
}
