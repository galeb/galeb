package io.galeb.legba.conversors;

import static com.google.common.hash.Hashing.sha256;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.galeb.core.entity.HealthStatus;
import io.galeb.core.entity.HealthStatus.Status;
import io.galeb.core.entity.Target;
import io.galeb.core.entity.VirtualHost;
import io.galeb.core.entity.WithStatus;
import io.galeb.legba.model.v1.RuleType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.util.StringUtils;

public class ConverterV1 implements Converter {

    // @formatter:off
    public static final  String API_VERSION                  = "v1";
    private static final String PROP_DISCOVERED_MEMBERS_SIZE = "discoveredMembersSize";
    public static final  String PROP_CONN_PER_THREAD         = "connPerThread";
    public static final  String FULLHASH_PROP                = "fullhash";
    // @formatter:on

    private final Gson gson = new GsonBuilder().serializeNulls().create();

    @Override
    public String convertToString(List<VirtualHost> virtualHostList, String numRouters, String version, String zoneId, Long envId) {
        List<io.galeb.legba.model.v1.VirtualHost> list = new ArrayList<>();
        List<String> keysFullHash = new ArrayList<>();
        virtualHostList.forEach(vh -> {
            keysFullHash.add(vh.getLastModifiedAt().toString());

            io.galeb.legba.model.v1.VirtualHost v = new io.galeb.legba.model.v1.VirtualHost();
            v.setName(vh.getName());
            v.setRules(convertVirtualhostGroup(vh.getVirtualhostgroup(), numRouters, zoneId, envId, keysFullHash));

            io.galeb.legba.model.v1.Environment environment = new io.galeb.legba.model.v1.Environment();
            io.galeb.core.entity.Environment env = vh.getEnvironments().stream().filter(e -> e.getId() == envId).findFirst().get();
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

    private Set<io.galeb.legba.model.v1.Rule> convertVirtualhostGroup(io.galeb.core.entity.VirtualhostGroup virtualhostgroup, String numRouters, String zoneId, Long envId, List<String> keysFullHash) {

        Set<io.galeb.legba.model.v1.Rule> rules = new HashSet<>();

        virtualhostgroup.getRulesordered().forEach(ro -> {
            keysFullHash.add(ro.getLastModifiedAt().toString());
            keysFullHash.add(ro.getRule().getLastModifiedAt().toString());

            io.galeb.legba.model.v1.Rule rule = new io.galeb.legba.model.v1.Rule();
            rule.setGlobal(ro.getRule().getGlobal());
            rule.setName(ro.getRule().getName());
            rule.setRuleType(new RuleType("UrlPath"));

            HashMap<String, String> properties = new HashMap<>();
            properties.put("match", ro.getRule().getMatching());
            properties.put("order", ro.getOrder().toString());
            rule.setProperties(properties);

            rule.setPool(convertPools(ro.getRule().getPools(), numRouters, zoneId, envId, keysFullHash));

            rules.add(rule);
        });

        return rules;
    }

    private io.galeb.legba.model.v1.Pool convertPools(Set<io.galeb.core.entity.Pool> pools, String numRouters, String zoneId, Long envId, List<String> keysFullHash) {
        io.galeb.legba.model.v1.Pool pool = new io.galeb.legba.model.v1.Pool();
        Set<io.galeb.legba.model.v1.Target> targets = new HashSet<>();
        pools.stream().filter(p -> p.getEnvironment().getId() == envId).forEach(p -> {
            keysFullHash.add(p.getLastModifiedAt().toString());

            pool.setName(p.getName());
            io.galeb.legba.model.v1.BalancePolicy tempBalancePolicy = new io.galeb.legba.model.v1.BalancePolicy();
            tempBalancePolicy.setName(p.getBalancepolicy().getName());
            pool.setBalancePolicy(tempBalancePolicy);

            if (p.getPoolSize() > -1) {
                pool.getProperties().put(PROP_CONN_PER_THREAD, String.valueOf(p.getPoolSize()));
                pool.getProperties().put(PROP_DISCOVERED_MEMBERS_SIZE, String.valueOf(numRouters));
            }

            p.getTargets().stream().filter(t -> !WithStatus.Status.DELETED.equals(t.getStatus())).forEach(t -> {
                final Set<HealthStatus> healthStatuses = t.getHealthStatus();
                if (healthStatuses == null || healthStatuses.isEmpty()) {
                    targets.add(newTargetV1(t, keysFullHash));
                } else {
                    healthStatuses.stream()
                        .filter(hs ->
                            (StringUtils.isEmpty(zoneId) || hs.getSource().equals(zoneId)) &&
                            hs.getStatus() != null ||
                            !Status.FAIL.equals(hs.getStatus()))
                        .forEach(hs -> {
                            keysFullHash.add(hs.getLastModifiedAt().toString());
                            targets.add(newTargetV1(t, keysFullHash));
                        });
                }
            });
            pool.setTargets(targets);
        });
        return pool;
    }

    private io.galeb.legba.model.v1.Target newTargetV1(final Target target, final List<String> keysFullHash) {
        io.galeb.legba.model.v1.Target targetV1 = new io.galeb.legba.model.v1.Target();
        targetV1.setName(target.getName());
        keysFullHash.add(target.getLastModifiedAt().toString());
        return targetV1;
    }
}
