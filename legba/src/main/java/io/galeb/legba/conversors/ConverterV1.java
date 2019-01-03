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

import static com.google.common.hash.Hashing.sha256;
import static io.galeb.core.entity.HealthStatus.Status.HEALTHY;
import static io.galeb.core.entity.HealthStatus.Status.UNKNOWN;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import io.galeb.core.enums.SystemEnv;
import io.galeb.core.log.JsonEventToLogger;
import io.galeb.legba.controller.RoutersController.RouterMeta;
import io.galeb.legba.model.v1.BalancePolicy;
import io.galeb.legba.model.v1.Environment;
import io.galeb.legba.model.v1.Pool;
import io.galeb.legba.model.v1.Rule;
import io.galeb.legba.model.v1.RuleType;
import io.galeb.legba.model.v1.Target;
import io.galeb.legba.model.v1.VirtualHost;
import io.galeb.legba.model.v2.QueryResultLine;
import io.galeb.legba.repository.VirtualHostRepository;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConverterV1 implements Converter {

    private static final boolean ENABLE_DISCOVERED_MEMBERS_SIZE = Boolean.valueOf(SystemEnv.ENABLE_DISCOVERED_MEMBERS_SIZE.getValue());

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

        final List<QueryResultLine> queryResultLines = getQueryResultLines(queryResultLinesObj);

        final Map<VirtualHost, String> virtualhostFullHash = new HashMap<>();
        long numVirtualhosts = queryResultLines.stream().map(QueryResultLine::getVirtualhostId).distinct().count();
        JsonEventToLogger event = new JsonEventToLogger(this.getClass());
        event.put("message", "Converting to string");
        event.put("envId", envId);
        event.put("numVirtualHost", String.valueOf(numVirtualhosts));
        event.put("numRouters", numRouters);
        event.put("correlation", routerMeta.correlation);
        event.sendInfo();

        Environment environmentV1 = new Environment();
        environmentV1.setId(envId);
        environmentV1.setProperties(Collections.singletonMap(FULLHASH_PROP, version));

        ObjectNode json = objectMapper.createObjectNode();
        final RuleType ruleType = new RuleType("UrlPath");
        final Map<String, BalancePolicy> balancePolicyMap = new HashMap<>();

        loopQueryResultLine: for (QueryResultLine queryResultLine: queryResultLines) {
            final Date virtualhostLastModifiedAt = queryResultLine.getVirtualhostLastModifiedAt();
            final Date ruleOrderedLastModifiedAt = queryResultLine.getRuleOrderedLastModifiedAt();
            final Date ruleLastModifiedAt = queryResultLine.getRuleLastModifiedAt();
            final Date poolLastModifiedAt = queryResultLine.getPoolLastModifiedAt();
            final Date targetLastModifiedAt = queryResultLine.getTargetLastModifiedAt();
            if (virtualhostLastModifiedAt == null || ruleOrderedLastModifiedAt == null ||
                ruleLastModifiedAt == null || poolLastModifiedAt == null || targetLastModifiedAt == null) {

                JsonEventToLogger eventAbortProcessing = new JsonEventToLogger(this.getClass());
                eventAbortProcessing.put("message", "Aborting convertToString. Mandatory attribute is NULL");
                eventAbortProcessing.put("virtualhostName", queryResultLine.getVirtualhostName());
                eventAbortProcessing.put("virtualhostLastModifiedAt", virtualhostLastModifiedAt == null ? "NULL" : virtualhostLastModifiedAt.toString());
                eventAbortProcessing.put("ruleOrderedLastModifiedAt", ruleOrderedLastModifiedAt == null ? "NULL" : ruleOrderedLastModifiedAt.toString());
                eventAbortProcessing.put("ruleLastModifiedAt", ruleLastModifiedAt == null ? "NULL" : ruleLastModifiedAt.toString());
                eventAbortProcessing.put("poolLastModifiedAt", poolLastModifiedAt == null ? "NULL" : poolLastModifiedAt.toString());
                eventAbortProcessing.put("targetLastModifiedAt", targetLastModifiedAt == null ? "NULL" : targetLastModifiedAt.toString());
                eventAbortProcessing.put("correlation", routerMeta.correlation);
                eventAbortProcessing.sendWarn();

                break loopQueryResultLine;
            }

            String virtualhostName = queryResultLine.getVirtualhostName();
            VirtualHost virtualhostV1 = null;
            loopVh: for (VirtualHost vhTemp: virtualhostFullHash.keySet()) {
                if (vhTemp.getName().equals(queryResultLine.getVirtualhostName())) {
                    virtualhostV1 = vhTemp;
                    break loopVh;
                }
            }
            if (virtualhostV1 == null) {
                virtualhostV1 = new VirtualHost();
                virtualhostV1.setName(virtualhostName);
                virtualhostV1.setEnvironment(environmentV1);
                virtualhostV1.setId(queryResultLine.getVirtualhostId());
            }
            calculeHash(virtualhostV1, queryResultLine, virtualhostFullHash, numRouters);

            Rule ruleV1 = null;
            loopRule: for (Rule ruleTemp: virtualhostV1.getRules()) {
                if (ruleTemp.getId() == queryResultLine.getRuleId()) {
                    ruleV1 = ruleTemp;
                    break loopRule;
                }
            }
            if (ruleV1 == null) {
                ruleV1 = new Rule();
                ruleV1.setId(queryResultLine.getRuleId());
                ruleV1.setName(queryResultLine.getRuleName());
                ruleV1.setGlobal(queryResultLine.getRuleGlobal());
                ruleV1.setRuleType(ruleType);
                ruleV1.setProperties(new HashMap<String, String>() {{
                    put("match", queryResultLine.getRuleMatching());
                    put("order", queryResultLine.getRuleOrderedOrder().toString());
                }});
                virtualhostV1.getRules().add(ruleV1);
            }

            Pool poolV1;
            if ((poolV1 = ruleV1.getPool()) == null) {
                poolV1 = new Pool();
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
                if (ENABLE_DISCOVERED_MEMBERS_SIZE) {
                    poolV1.setProperties(new HashMap<String, String>() {{
                        put(PROP_CONN_PER_THREAD, String.valueOf(queryResultLine.getPoolSize()));
                        put(PROP_DISCOVERED_MEMBERS_SIZE, String.valueOf(numRouters));
                    }});
                }
                ruleV1.setPool(poolV1);
            }

            Target targetV1 = null;
            loopTarget: for (Target targetTemp: poolV1.getTargets()) {
                if (targetTemp.getId() == queryResultLine.getTargetId()) {
                    targetV1 = targetTemp;
                    break loopTarget;
                }
            }
            if (targetV1 == null) {
                targetV1 = new Target();
                targetV1.setId(queryResultLine.getTargetId());
                targetV1.setName(queryResultLine.getTargetName());
            }

            if (canSendTargetToRoute(queryResultLine.getHealthStatusStatus())) {
                poolV1.getTargets().add(targetV1);
            }
        }

        final ArrayNode virtualHostsV1 = json.putArray("virtualhosts");
        for (VirtualHost virtualHost: virtualhostFullHash.keySet()) {
            String rawFullHash = virtualhostFullHash.get(virtualHost);
            virtualHost.setProperties(Collections.singletonMap(FULLHASH_PROP, makeHash(rawFullHash)));
            virtualHostsV1.add(objectMapper.convertValue(virtualHost, JsonNode.class));
        }
        return json.toString();
    }

    public String makeHash(String rawFullHash) {
        return sha256().hashString(rawFullHash, Charsets.UTF_8).toString();
    }

    public List<QueryResultLine> getQueryResultLines(final List<Object[]> queryResultLinesObj) {
        return queryResultLinesObj.stream().map(QueryResultLine::new).collect(Collectors.toList());
    }

    public Map<VirtualHost, String> calculeHash(
        final VirtualHost virtualHostV1,
        final QueryResultLine queryResultLine,
        final Map<VirtualHost, String> virtualhostFullHash,
        int numRouters) {

        String lastFullHash = virtualhostFullHash.get(virtualHostV1);
        String fullHash = (lastFullHash != null ? lastFullHash : "") +
            queryResultLine.getVirtualhostLastModifiedAt().toString() +
            queryResultLine.getRuleOrderedLastModifiedAt().toString() +
            queryResultLine.getRuleLastModifiedAt().toString() +
            queryResultLine.getPoolLastModifiedAt().toString() +
            queryResultLine.getTargetLastModifiedAt().toString() +
            (queryResultLine.getHealthStatusLastModifiedAt() != null ? queryResultLine.getHealthStatusLastModifiedAt() : "NULL") +
            (ENABLE_DISCOVERED_MEMBERS_SIZE ? String.valueOf(queryResultLine.getPoolSize()) + numRouters : "DISCOVERED_MEMBERS_SIZE_DISABLED");

        virtualhostFullHash.put(virtualHostV1, fullHash);

        return virtualhostFullHash;
    }

    @Override
    public boolean canSendTargetToRoute(String healthStatus) {
        return healthStatus == null || healthStatus.contains(HEALTHY.name()) || healthStatus.contains(UNKNOWN.name());
    }

    @Override
    public String getApiVersion() {
        return API_VERSION;
    }
}
