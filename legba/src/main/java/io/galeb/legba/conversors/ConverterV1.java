package io.galeb.legba.conversors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.galeb.core.entity.HealthStatus;
import io.galeb.core.entity.VirtualHost;

import java.util.*;
import java.util.stream.Collectors;

public class ConverterV1 implements Converter {

    public static final String API_VERSION = "v1";
    private static final String PROP_DISCOVERED_MEMBERS_SIZE = "discoveredMembersSize";

    private final Gson gson = new GsonBuilder().serializeNulls().create();

    @Override
    public String convertToString(List<VirtualHost> virtualHostList, String numRouters, String version, String networkId, String envId) {
        List<io.galeb.legba.model.v1.VirtualHost> list = new ArrayList<>();
        virtualHostList.stream().forEach(vh -> {
            io.galeb.legba.model.v1.VirtualHost v = new io.galeb.legba.model.v1.VirtualHost();
            v.setName(vh.getName());
            v.setRules(convertVirtualhostGroup(vh.getVirtualhostgroup(), numRouters, networkId, envId));
            list.add(v);

        });
        Map<String, List<io.galeb.legba.model.v1.VirtualHost>> newList = new HashMap<>();
        newList.put("virtualhosts", list);
        return gson.toJson(newList);
    }

    private Set<io.galeb.legba.model.v1.Rule> convertVirtualhostGroup(io.galeb.core.entity.VirtualhostGroup virtualhostgroup, String numRouters, String networkId, String envId) {

        Set<io.galeb.legba.model.v1.Rule> rules = new HashSet<>();

        virtualhostgroup.getRulesordered().stream().forEach(ro -> {
            io.galeb.legba.model.v1.Rule rule = new io.galeb.legba.model.v1.Rule();
            rule.setGlobal(ro.getRule().getGlobal());
            rule.setName(ro.getRule().getName());
            HashMap<String, String> properties = new HashMap();
            properties.put("match", ro.getRule().getMatching());
            properties.put("order", ro.getOrder().toString());
            rule.setProperties(properties);
            rule.setPool(convertPools(ro.getRule().getPools(), numRouters, networkId, envId));

            rules.add(rule);
        });

        return rules;
    }

    private io.galeb.legba.model.v1.Pool convertPools(Set<io.galeb.core.entity.Pool> pools, String numRouters, String networkId, String envId) {
        io.galeb.legba.model.v1.Pool pool = new io.galeb.legba.model.v1.Pool();
        Set<io.galeb.legba.model.v1.Target> targets = new HashSet<>();
        pools.stream().filter(p -> p.getEnvironment().getId() == Long.valueOf(envId)).forEach(p -> {
            pool.setName(p.getName());
            io.galeb.legba.model.v1.BalancePolicy tempBalancePolicy = new io.galeb.legba.model.v1.BalancePolicy();
            tempBalancePolicy.setName(p.getBalancepolicy().getName());
            pool.setBalancePolicy(tempBalancePolicy);

            /**
             * Commented because today not contains the use of numRouters
             */
            //pool.getProperties().put(PROP_DISCOVERED_MEMBERS_SIZE, String.valueOf(numRouters));

            p.getTargets().stream().forEach(t -> {
                Set<HealthStatus> healthStatusesOK = t.getHealthStatus()
                        .stream()
                        .filter(hs -> (networkId == null || hs.getSource().equals(networkId)) && hs.getStatus().equals(HealthStatus.Status.HEALTHY))
                        .collect(Collectors.toSet());
                healthStatusesOK.stream().forEach(hs -> {
                    io.galeb.legba.model.v1.Target target = new io.galeb.legba.model.v1.Target();
                    target.setName(hs.getTarget().getName());
                    targets.add(target);
                });
            });
            pool.setTargets(targets);
        });
        return pool;
    }
}
