/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
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

package io.galeb.router.tests.mocks;

import static io.galeb.core.enums.EnumHealthState.OK;
import static io.galeb.core.enums.EnumPropHealth.PROP_HEALTHY;
import static io.galeb.router.configurations.ManagerClientCacheConfiguration.FULLHASH_PROP;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.galeb.core.entity.BalancePolicy;
import io.galeb.core.entity.BalancePolicyType;
import io.galeb.core.entity.Environment;
import io.galeb.core.entity.Pool;
import io.galeb.core.entity.Project;
import io.galeb.core.entity.Rule;
import io.galeb.core.entity.RuleType;
import io.galeb.core.entity.Target;
import io.galeb.core.entity.VirtualHost;
import io.galeb.core.enums.EnumRuleType;
import io.galeb.router.client.hostselectors.HostSelectorLookup;
import io.galeb.router.handlers.builder.HandlerBuilder;
import io.galeb.router.sync.ManagerClient;

@Configuration
@Profile({ "test" })
public class ManagerClientConfigurationMock {

    private static final Logger logger = LoggerFactory.getLogger(ManagerClientConfigurationMock.class);

    public enum ManagerResponse {
        CONFIG_DEFAULT(buildResponse(new String[] { "test.com" }, null)),
        CONFIG_ALIAS(buildResponse(new String[] { "test.com" }, new String[] { "alias.test.com" })),
        CONFIG_MULTIVHOSTS(buildResponse(new String[] { "test.com", "other.com" }, null));

        final ManagerClient.Virtualhosts vhosts;

        final ManagerClient.Virtualhosts getVHosts() {
            return this.vhosts;
        }

        ManagerResponse(final ManagerClient.Virtualhosts vhosts) {
            this.vhosts = vhosts;
        }
    }

    private static String vhostHash(String originalString) {
        String sha256hex = Hashing.sha256().hashString(originalString, StandardCharsets.UTF_8).toString();
        return sha256hex;
    }

    private static VirtualHost createVirtualHost(Environment env, Project p, String hostname, String[] aliases) {
        VirtualHost virtualhost = new VirtualHost(hostname, env, p);
        RandomDataGenerator rdg = new RandomDataGenerator();
        virtualhost.setHash(rdg.nextInt(10, 200 ));
        virtualhost.setId(rdg.nextLong(10L, 200L));

        Map<String, String> virtualhostProperties = new HashMap<>();
        virtualhostProperties.put(HandlerBuilder.IPACL_ALLOW,
                "127.0.0.0/8,0:0:0:0:0:0:0:1/128,10.*.*.*,172.*.*.*,192.168.*.*");
        virtualhostProperties.put(FULLHASH_PROP, vhostHash(hostname + (aliases == null ? "-alias-" : aliases.toString())));
        virtualhost.setProperties(virtualhostProperties);

        if (aliases != null) {
            Set<String> a = Sets.newHashSet(aliases);
            virtualhost.setAliases(a);
        }

        logger.info("vhost: "+ hostname + " id: " + virtualhost.getId() + " hash: " + virtualhost.getHash() + " hashCode: " + virtualhost.hashCode());

        return virtualhost;
    }

    private static Pool createPool(HostSelectorLookup balancePolicy) {
        Target target = new Target("http://127.0.0.1:8080");
        Map<String, String> targetProperties = new HashMap<>();
        targetProperties.put(PROP_HEALTHY.value(), OK.toString());
        target.setProperties(targetProperties);

        BalancePolicy balancePolicyRR = new BalancePolicy(balancePolicy.toString(),
                new BalancePolicyType(balancePolicy.toString()));

        Pool pool = new Pool("pool_test");
        pool.setTargets(Collections.singleton(target));
        pool.setBalancePolicy(balancePolicyRR);
        return pool;
    }

    private static Rule createRule(RuleType rt, Pool p, String name, String match, int order) {
        Rule r = new Rule(name, rt, p);
        Map<String, String> ruleProperties = new HashMap<>();
        ruleProperties.put(HandlerBuilder.RULE_MATCH, match);
        ruleProperties.put(HandlerBuilder.RULE_ORDER, Integer.toString(order));
        r.setProperties(ruleProperties);
        return r;
    }

    private static ManagerClient.Virtualhosts buildResponse(String[] hosts, String[] aliases) {
        Project project = new Project("projectX");
        Environment env = new Environment("desenv");
        env.getProperties().put(FULLHASH_PROP, UUID.randomUUID().toString());

        RuleType ruleType = new RuleType(EnumRuleType.PATH.toString());
        Pool pool = createPool(HostSelectorLookup.ROUNDROBIN);
        Rule rule_slash = createRule(ruleType, pool, "rule_test_slash", "/", Integer.MAX_VALUE - 1);
        Rule other_rule = createRule(ruleType, pool, "other_rule", "/search", 0);

        ManagerClient.Virtualhosts virtualhostsFromManager = new ManagerClient.Virtualhosts();
        virtualhostsFromManager.virtualhosts = new VirtualHost[hosts.length];

        for (int i = 0; i < hosts.length; i++) {
            VirtualHost virtuahost = createVirtualHost(env, project, hosts[i], aliases);
            virtuahost.setRules(new HashSet<>(Arrays.asList(rule_slash, other_rule)));

            virtualhostsFromManager.virtualhosts[i] = virtuahost;
        }
        return virtualhostsFromManager;
    }

    private ManagerResponse currentResponse = ManagerResponse.CONFIG_DEFAULT;

    public synchronized void setResponse(ManagerResponse resp) {
        currentResponse = resp;
    }

    @Bean
    ManagerClient managerClient() {
        logger.warn("ManagerClientConfiguration " + this);

        return new ManagerClient() {
            @Override
            public void getVirtualhosts(String envname, String etag, ResultCallBack resultCallBack) {
                logger.warn("getVirtualhosts(" + envname + ", " + etag + ", " + resultCallBack);
                logger.warn("current response is: " + currentResponse.name());
                resultCallBack.onResult(200, currentResponse.getVHosts());
            }

            @Override
            public void register(String etag) {
                logger.info("sending POST to Manager (ignored) with etag " + etag);
            }
        };
    }
}
