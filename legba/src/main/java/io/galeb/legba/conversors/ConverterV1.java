package io.galeb.legba.conversors;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.galeb.core.entity.HealthStatus;
import io.galeb.core.entity.VirtualHost;
import io.galeb.legba.model.v1.RuleType;

import static com.google.common.hash.Hashing.sha256;

import java.util.*;
import java.util.stream.Collectors;

public class ConverterV1 implements Converter {

    public static final String API_VERSION = "v1";
    private static final String PROP_DISCOVERED_MEMBERS_SIZE = "discoveredMembersSize";
    public static final String PROP_CONN_PER_THREAD         = "connPerThread";
    public static final String FULLHASH_PROP = "fullhash";

    private final Gson gson = new GsonBuilder().serializeNulls().create();

    @Override
    public String convertToString(List<VirtualHost> virtualHostList, String numRouters, String version, String zoneId, String envId) {
        List<io.galeb.legba.model.v1.VirtualHost> list = new ArrayList<>();
        List<String> keysFullHash = new ArrayList<>();
        virtualHostList.stream().forEach(vh -> {
            keysFullHash.add(vh.getLastModifiedAt().toString());

            io.galeb.legba.model.v1.VirtualHost v = new io.galeb.legba.model.v1.VirtualHost();
            v.setName(vh.getName());
            v.setRules(convertVirtualhostGroup(vh.getVirtualhostgroup(), numRouters, zoneId, envId, keysFullHash));

            io.galeb.legba.model.v1.Environment environment = new io.galeb.legba.model.v1.Environment();
            io.galeb.core.entity.Environment env = vh.getEnvironments().stream().filter(e -> e.getId() == Long.valueOf(envId)).findFirst().get();
            environment.setId(env.getId());

            Map<String, String> map = new HashMap<>();
            map.put(FULLHASH_PROP, version);
            environment.setProperties(map);
            v.setEnvironment(environment);

            Map<String, String> mapVh = new HashMap<>();
            String key = String.join("", keysFullHash);
            String fullHashVH =  sha256().hashString(key, Charsets.UTF_8).toString();
            mapVh.put(FULLHASH_PROP, fullHashVH);
            v.setProperties(mapVh);

            list.add(v);

        });
        Map<String, List<io.galeb.legba.model.v1.VirtualHost>> newList = new HashMap<>();
        newList.put("virtualhosts", list);
        return gson.toJson(newList);
    }

    private Set<io.galeb.legba.model.v1.Rule> convertVirtualhostGroup(io.galeb.core.entity.VirtualhostGroup virtualhostgroup, String numRouters, String zoneId, String envId, List<String> keysFullHash) {

        Set<io.galeb.legba.model.v1.Rule> rules = new HashSet<>();

        virtualhostgroup.getRulesordered().stream().forEach(ro -> {
            keysFullHash.add(ro.getLastModifiedAt().toString());
            keysFullHash.add(ro.getRule().getLastModifiedAt().toString());

            io.galeb.legba.model.v1.Rule rule = new io.galeb.legba.model.v1.Rule();
            rule.setGlobal(ro.getRule().getGlobal());
            rule.setName(ro.getRule().getName());
            rule.setRuleType(new RuleType("UrlPath"));

            HashMap<String, String> properties = new HashMap();
            properties.put("match", ro.getRule().getMatching());
            properties.put("order", ro.getOrder().toString());
            rule.setProperties(properties);

            rule.setPool(convertPools(ro.getRule().getPools(), numRouters, zoneId, envId, keysFullHash));

            rules.add(rule);
        });

        return rules;
    }

    private io.galeb.legba.model.v1.Pool convertPools(Set<io.galeb.core.entity.Pool> pools, String numRouters, String zoneId, String envId, List<String> keysFullHash) {
        io.galeb.legba.model.v1.Pool pool = new io.galeb.legba.model.v1.Pool();
        Set<io.galeb.legba.model.v1.Target> targets = new HashSet<>();
        pools.stream().filter(p -> p.getEnvironment().getId() == Long.valueOf(envId)).forEach(p -> {
            keysFullHash.add(p.getLastModifiedAt().toString());

            pool.setName(p.getName());
            io.galeb.legba.model.v1.BalancePolicy tempBalancePolicy = new io.galeb.legba.model.v1.BalancePolicy();
            tempBalancePolicy.setName(p.getBalancepolicy().getName());
            pool.setBalancePolicy(tempBalancePolicy);

            if (p.getPoolSize() > -1) {
                pool.getProperties().put(PROP_CONN_PER_THREAD, String.valueOf(p.getPoolSize()));
                pool.getProperties().put(PROP_DISCOVERED_MEMBERS_SIZE, String.valueOf(numRouters));
            }

            p.getTargets().stream().forEach(t -> {
                Set<HealthStatus> healthStatusesOK = t.getHealthStatus()
                        .stream()
                        .filter(hs -> (zoneId == null || hs.getSource().equals(zoneId)) && hs.getStatus().equals(HealthStatus.Status.HEALTHY))
                        .collect(Collectors.toSet());
                healthStatusesOK.stream().forEach(hs -> {
                    keysFullHash.add(hs.getLastModifiedAt().toString());
                    keysFullHash.add(hs.getTarget().getLastModifiedAt().toString());
                    
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
