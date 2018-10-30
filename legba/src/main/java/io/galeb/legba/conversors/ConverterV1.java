package io.galeb.legba.conversors;

import static com.google.common.hash.Hashing.sha256;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.galeb.core.entity.HealthStatus;
import io.galeb.core.entity.HealthStatus.Status;
import io.galeb.core.entity.Pool;
import io.galeb.core.entity.Rule;
import io.galeb.core.entity.RuleOrdered;
import io.galeb.core.entity.Target;
import io.galeb.core.entity.VirtualHost;
import io.galeb.core.entity.VirtualhostGroup;
import io.galeb.core.entity.WithStatus;
import io.galeb.core.log.JsonEventToLogger;
import io.galeb.legba.model.v1.RuleType;
import io.galeb.legba.repository.VirtualHostRepository;
import io.galeb.legba.services.RoutersService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConverterV1 implements Converter {

    // @formatter:off
    public static final  String API_VERSION                  = "v1";
    private static final String PROP_DISCOVERED_MEMBERS_SIZE = "discoveredMembersSize";
    public static final  String PROP_CONN_PER_THREAD         = "connPerThread";
    public static final  String FULLHASH_PROP                = "fullhash";
    // @formatter:on

    private final Gson gson = new GsonBuilder().serializeNulls().create();
    private final VirtualHostRepository virtualHostRepository;
    private final RoutersService routersService;

    @Autowired
    public ConverterV1(VirtualHostRepository virtualHostRepository, RoutersService routersService) {
        this.virtualHostRepository = virtualHostRepository;
        this.routersService = routersService;
    }

    @Override
    public String convertToString(String logCorrelation, String version, String zoneId, Long envId, String groupId) {
        final List<io.galeb.legba.model.v1.VirtualHost> virtualHostsV1 = new ArrayList<>();
        final List<VirtualHost> virtualHostsV2 = virtualHostRepository.findAllByEnvironmentId(envId);
        int numRouters = routersService.get(envId.toString(), groupId);

        JsonEventToLogger event = new JsonEventToLogger(this.getClass());
        event.put("numVirtualHost", String.valueOf(virtualHostsV2.size()));
        event.put("numRouters", numRouters);
        event.put("correlation", logCorrelation);
        event.sendInfo();

        io.galeb.legba.model.v1.Environment environmentV1 = new io.galeb.legba.model.v1.Environment();
        environmentV1.setId(envId);
        environmentV1.setProperties(Collections.singletonMap(FULLHASH_PROP, version));

        for (VirtualHost vh2: virtualHostsV2) {
            final List<String> keysFullHash = new ArrayList<>();
            keysFullHash.add(vh2.getLastModifiedAt().toString());
            final io.galeb.legba.model.v1.VirtualHost vh1 = new io.galeb.legba.model.v1.VirtualHost();
            vh1.setName(vh2.getName());
            final VirtualhostGroup virtualhostgroupV2 = vh2.getVirtualhostgroup();
            final Set<RuleOrdered> rulesorderedV2 = virtualhostgroupV2.getRulesordered();
            vh1.setRules(convertVirtualhostGroup(rulesorderedV2, numRouters, zoneId, envId, keysFullHash));
            vh1.setEnvironment(environmentV1);
            String key = String.join("", keysFullHash);
            String fullHashVH =  sha256().hashString(key, Charsets.UTF_8).toString();
            vh1.setProperties(Collections.singletonMap(FULLHASH_PROP, fullHashVH));

            virtualHostsV1.add(vh1);
        }
        return gson.toJson(Collections.singletonMap("virtualhosts", virtualHostsV1));
    }

    private Set<io.galeb.legba.model.v1.Rule> convertVirtualhostGroup(Set<RuleOrdered> rulesordered, int numRouters,
        String zoneId, Long envId, List<String> keysFullHash) {

        final Set<io.galeb.legba.model.v1.Rule> rulesV1 = new HashSet<>();
        final RuleType ruleType = new RuleType("UrlPath");

        for (RuleOrdered ruleOrdered: rulesordered) {
            keysFullHash.add(ruleOrdered.getLastModifiedAt().toString());
            Rule ruleV2 = ruleOrdered.getRule();
            keysFullHash.add(ruleV2.getLastModifiedAt().toString());

            io.galeb.legba.model.v1.Rule ruleV1 = new io.galeb.legba.model.v1.Rule();
            ruleV1.setGlobal(ruleV2.getGlobal());
            ruleV1.setName(ruleV2.getName());
            ruleV1.setRuleType(ruleType);

            ruleV1.setProperties(new HashMap<String, String>(){{
                put("match", ruleV2.getMatching());
                put("order", ruleOrdered.getOrder().toString());
            }});
            ruleV1.setPool(convertPools(ruleV2.getPools(), numRouters, zoneId, envId, keysFullHash));
            rulesV1.add(ruleV1);
        }

        return rulesV1;
    }

    private io.galeb.legba.model.v1.Pool convertPools(Set<io.galeb.core.entity.Pool> pools, int numRouters,
        String zoneId, Long envId, List<String> keysFullHash) {

        io.galeb.legba.model.v1.Pool poolV1 = new io.galeb.legba.model.v1.Pool();
        Set<io.galeb.legba.model.v1.Target> targetsV1 = new HashSet<>();

        long tempPoolSizeV1 = -1L;
        for (Pool poolV2: pools) {
            if (poolV2.getEnvironment().getId() != envId) {
                continue;
            }
            keysFullHash.add(poolV2.getLastModifiedAt().toString());

            poolV1.setName(poolV2.getName());
            io.galeb.legba.model.v1.BalancePolicy tempBalancePolicy = new io.galeb.legba.model.v1.BalancePolicy();
            tempBalancePolicy.setName(poolV2.getBalancepolicy().getName());
            poolV1.setBalancePolicy(tempBalancePolicy);

            long poolSizeV2 = poolV2.getPoolSize();
            if (poolSizeV2 > -1L) {
                tempPoolSizeV1 = Math.max(tempPoolSizeV1, poolSizeV2);
            }

            final Set<Target> targetsV2 = poolV2.getTargets();
            for (Target targetV2: targetsV2) {
                if (WithStatus.Status.DELETED.equals(targetV2.getStatus().get(envId))) {
                    continue;
                }
                keysFullHash.add(targetV2.getLastModifiedAt().toString());
                final Set<HealthStatus> healthStatuses = targetV2.getHealthStatus();
                if (healthStatuses == null || healthStatuses.isEmpty()) {
                    targetsV1.add(newTargetV1(targetV2));
                } else {
                    healthStatuses.stream()
                        .filter(hs ->
                            (StringUtils.isEmpty(zoneId) || hs.getSource().equals(zoneId)) &&
                            hs.getStatus() != null ||
                            !Status.FAIL.equals(hs.getStatus()))
                        .forEach(hs -> {
                            keysFullHash.add(hs.getLastModifiedAt().toString());
                            targetsV1.add(newTargetV1(targetV2));
                        });
                }
            }
        }
        if (tempPoolSizeV1 > -1L) {
            final long poolSizeV1 = tempPoolSizeV1;
            poolV1.setProperties(new HashMap<String, String>(){{
                put(PROP_CONN_PER_THREAD, String.valueOf(poolSizeV1));
                put(PROP_DISCOVERED_MEMBERS_SIZE, String.valueOf(numRouters));
            }});
        }
        return poolV1.setTargets(targetsV1);
    }

    private io.galeb.legba.model.v1.Target newTargetV1(final Target target) {
        io.galeb.legba.model.v1.Target targetV1 = new io.galeb.legba.model.v1.Target();
        targetV1.setName(target.getName());
        return targetV1;
    }

    @Override
    public String getApiVersion() {
        return API_VERSION;
    }
}
