package io.galeb.legba.conversors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.galeb.core.entity.VirtualHost;
import io.galeb.legba.model.v1.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConverterV1 implements Converter {

    public static final String API_VERSION = "v1";

    private final Gson gson = new GsonBuilder().serializeNulls().create();

    @Override
    public String convertToString(List<VirtualHost> virtualHostList, String numRouters, String version) {
        List<io.galeb.legba.model.v1.VirtualHost> list = new ArrayList<>();
        virtualHostList.stream().forEach(vh -> {

            io.galeb.legba.model.v1.VirtualHost v = new io.galeb.legba.model.v1.VirtualHost();
            v.setName(vh.getName());
            v.setVersion(version);
            v.setVirtualhostGroup(convertVirtualhostGroup(vh.getVirtualhostgroup(), numRouters));
            list.add(v);

        });
        return gson.toJson(list);
    }

    private VirtualhostGroup convertVirtualhostGroup(io.galeb.core.entity.VirtualhostGroup virtualhostgroup, String numRouters) {

        VirtualhostGroup vhg = new VirtualhostGroup();
        Set<RuleOrdered> ruleOrdereds = new HashSet<>();
        virtualhostgroup.getRulesordered().stream().forEach(ro -> {
            RuleOrdered roLocal = new RuleOrdered();
            roLocal.setOrder(ro.getOrder());

            Rule rule = new Rule();
            rule.setGlobal(ro.getRule().getGlobal());
            rule.setMatching(ro.getRule().getMatching());
            rule.setName(ro.getRule().getMatching());
            rule.setPools(convertPools(ro.getRule().getPools(), numRouters));

            roLocal.setRule(rule);
            ruleOrdereds.add(roLocal);
        });

        vhg.setRulesordered(ruleOrdereds);
        return vhg;
    }

    private Set<Pool> convertPools(Set<io.galeb.core.entity.Pool> pools, String numRouters) {
        Set<Pool> poolsLocal = new HashSet<>();
        pools.stream().forEach(p -> {
            Pool pool = new Pool();
            pool.setName(p.getName());
            pool.setDiscoveredMembersSize(numRouters);

            Set<Target> targets = new HashSet<>();
            p.getTargets().stream().forEach(t -> {
                Target target = new Target();
                target.setName(t.getName());
                targets.add(target);
            });
            pool.setTargets(targets);
            poolsLocal.add(pool);
        });
        return poolsLocal;
    }
}
