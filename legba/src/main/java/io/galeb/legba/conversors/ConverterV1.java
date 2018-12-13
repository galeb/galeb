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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import io.galeb.core.log.JsonEventToLogger;
import io.galeb.legba.controller.RoutersController.RouterMeta;
import io.galeb.legba.model.v1.BalancePolicy;
import io.galeb.legba.model.v1.RuleType;
import io.galeb.legba.model.v1.VirtualHost;
import io.galeb.legba.model.v2.QueryResultLine;
import io.galeb.legba.repository.VirtualHostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.hash.Hashing.sha256;
import static io.galeb.core.entity.HealthStatus.Status.HEALTHY;
import static io.galeb.core.entity.HealthStatus.Status.UNKNOWN;

@Component
public class ConverterV1 implements Converter {

    // @formatter:off
    public static final  String API_VERSION                  = "v1";
    private static final String PROP_DISCOVERED_MEMBERS_SIZE = "discoveredMembersSize";
    private static final String PROP_CONN_PER_THREAD         = "connPerThread";
    private static final String FULLHASH_PROP                = "fullhash";
    // @formatter:on

    private final VirtualHostRepository virtualHostRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ConverterV1(VirtualHostRepository virtualHostRepository) {
        this.virtualHostRepository = virtualHostRepository;
    }

    @Override
    public String convertToString(final RouterMeta routerMeta, int numRouters, String version) {
        long envId = Long.parseLong(routerMeta.envId);
        String zoneId = routerMeta.zoneId;

        final List<Object[]> queryResultLinesObj = (zoneId == null) ?
            virtualHostRepository.fullEntityZoneIdNull(envId) :
            virtualHostRepository.fullEntity(envId, zoneId);

        final List<QueryResultLine> queryResultLines = queryResultLinesObj.stream().map(QueryResultLine::new).collect(Collectors.toList());

        final Map<VirtualHost, String> virtualhostFullHash = new HashMap<>();
        long numVirtualhosts = queryResultLines.stream().map(QueryResultLine::getVirtualhostId).distinct().count();
        JsonEventToLogger event = new JsonEventToLogger(this.getClass());
        event.put("message", "Converting to string");
        event.put("envId", envId);
        event.put("numVirtualHost", String.valueOf(numVirtualhosts));
        event.put("numRouters", numRouters);
        event.put("correlation", routerMeta.correlation);
        event.sendInfo();

        io.galeb.legba.model.v1.Environment environmentV1 = new io.galeb.legba.model.v1.Environment();
        environmentV1.setId(envId);
        environmentV1.setProperties(Collections.singletonMap(FULLHASH_PROP, version));

        ObjectNode json = objectMapper.createObjectNode();
        final RuleType ruleType = new RuleType("UrlPath");
        final Map<String, BalancePolicy> balancePolicyMap = new HashMap<>();

        for (QueryResultLine queryResultLine: queryResultLines) {
            Optional<VirtualHost> vh1Optional;
            final VirtualHost vh1;
            if (!(vh1Optional = virtualhostFullHash.keySet().stream()
                    .filter(v -> v.getId() == queryResultLine.getVirtualhostId()).findAny()).isPresent()) {
                vh1 = new VirtualHost();
                vh1.setName(queryResultLine.getVirtualhostName());
                vh1.setEnvironment(environmentV1);
                vh1.setId(queryResultLine.getVirtualhostId());
                String lastKetFullHash = virtualhostFullHash.get(vh1);
                virtualhostFullHash.put(vh1, lastKetFullHash + queryResultLine.getVirtualhostLastModifiedAt().toString());
            } else {
                vh1 = vh1Optional.get();
            }

            final Optional<io.galeb.legba.model.v1.Rule> ruleOptional = vh1.getRules().stream()
                    .filter(r -> r.getName().equals(queryResultLine.getRuleName())).findAny();
            io.galeb.legba.model.v1.Rule ruleV1;
            if (ruleOptional.isPresent()) {
                ruleV1 = ruleOptional.get();
            } else {
                ruleV1 = new io.galeb.legba.model.v1.Rule();
                ruleV1.setName(queryResultLine.getRuleName());
                ruleV1.setGlobal(queryResultLine.getRuleGlobal());
                ruleV1.setRuleType(ruleType);
                ruleV1.setProperties(new HashMap<String, String>() {{
                    put("match", queryResultLine.getRuleMatching());
                    put("order", queryResultLine.getRuleOrderedOrder().toString());
                }});
                String lastKetFullHash = virtualhostFullHash.get(vh1);
                virtualhostFullHash.put(vh1, lastKetFullHash + queryResultLine.getRuleLastModifiedAt().toString());
            }

            io.galeb.legba.model.v1.Pool poolV1;
            if ((poolV1 = ruleV1.getPool()) == null) {
                poolV1 = new io.galeb.legba.model.v1.Pool();
                poolV1.setName(queryResultLine.getPoolName());
                String balancePolicyName = queryResultLine.getBalancePolicyName();
                if (balancePolicyName != null) {
                    BalancePolicy balancePolicy;
                    if ((balancePolicy = balancePolicyMap.get(balancePolicyName)) == null) {
                        balancePolicy = new BalancePolicy();
                        balancePolicy.setName(balancePolicyName);
                        balancePolicyMap.put(balancePolicyName, balancePolicy);
                    }
                    poolV1.setBalancePolicy(balancePolicy);
                }
                poolV1.setProperties(new HashMap<String, String>() {{
                    put(PROP_CONN_PER_THREAD, String.valueOf(queryResultLine.getpPoolSize()));
                    put(PROP_DISCOVERED_MEMBERS_SIZE, String.valueOf(numRouters));
                }});
                String lastKetFullHash = virtualhostFullHash.get(vh1);
                virtualhostFullHash.put(vh1, lastKetFullHash + queryResultLine.getPoolLastModifiedAt().toString());
            }

            io.galeb.legba.model.v1.Target targetV1 = poolV1.getTargets().stream().filter(t -> t.getName().equals(queryResultLine.getTargetName()))
                    .findAny().orElseGet(() -> {
                        if (queryResultLine.getHealthStatusStatus() == null ||
                            queryResultLine.getHealthStatusStatus().contains(HEALTHY.name()) ||
                            queryResultLine.getHealthStatusStatus().contains(UNKNOWN.name())) {
                            return (io.galeb.legba.model.v1.Target) new io.galeb.legba.model.v1.Target().setName(queryResultLine.getTargetName());
                        }
                        return null;
                    });

            if (targetV1 != null && poolV1.getTargets().add(targetV1)) {
                String lastKetFullHash = virtualhostFullHash.get(vh1);
                virtualhostFullHash.put(vh1, lastKetFullHash + queryResultLine.getTargetLastModifiedAt().toString());
                if (queryResultLine.getHealthStatusLastModifiedAt() != null) {
                    lastKetFullHash = virtualhostFullHash.get(vh1);
                    virtualhostFullHash.put(vh1, lastKetFullHash + queryResultLine.getHealthStatusLastModifiedAt());
                }
            }
            ruleV1.setPool(poolV1);

            if (vh1.getRules().add(ruleV1)) {
                String lastKetFullHash = virtualhostFullHash.get(vh1);
                virtualhostFullHash.put(vh1, lastKetFullHash + queryResultLine.getRuleLastModifiedAt().toString());
            }
        }

        final ArrayNode virtualHostsV1 = json.putArray("virtualhosts");
        for (VirtualHost virtualHost: virtualhostFullHash.keySet()) {
            String key = virtualhostFullHash.get(virtualHost);
            String fullHashVH = sha256().hashString(key, Charsets.UTF_8).toString();
            virtualHost.setProperties(Collections.singletonMap(FULLHASH_PROP, fullHashVH));
            virtualHostsV1.add(objectMapper.convertValue(virtualHost, JsonNode.class));
        }
        return json.toString();
    }

    @Override
    public String getApiVersion() {
        return API_VERSION;
    }
}
