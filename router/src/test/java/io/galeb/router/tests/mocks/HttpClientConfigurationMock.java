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

import com.google.gson.Gson;
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
import io.galeb.core.enums.SystemEnv;
import io.galeb.router.client.hostselectors.HostSelectorLookup;
import io.galeb.router.sync.HttpClient;
import io.galeb.router.sync.ManagerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.*;

import static io.galeb.core.enums.EnumHealthState.OK;
import static io.galeb.core.enums.EnumPropHealth.PROP_HEALTHY;
import static io.galeb.router.handlers.NameVirtualHostDefaultHandler.IPACL_ALLOW;
import static io.galeb.router.handlers.RuleTargetHandler.RULE_MATCH;
import static io.galeb.router.handlers.RuleTargetHandler.RULE_ORDER;
import static io.galeb.router.sync.Updater.FULLHASH_PROP;

@Configuration
@Profile({ "test" })
public class HttpClientConfigurationMock {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Bean
    HttpClient httpClientService() {
        logger.warn("Using " + this);

        return new HttpClient() {

            @Override
            public void getResponseBody(String url, String etag, OnCompletedCallBack callBack) {
                if (url != null && url.startsWith(SystemEnv.MANAGER_URL.getValue() + "/virtualhostscached/")) {
                    Environment environment = new Environment("desenv");
                    environment.getProperties().put(FULLHASH_PROP, UUID.randomUUID().toString());
                    Project project = new Project("projectX");
                    VirtualHost virtuahost = new VirtualHost("test.com", environment, project);
                    Map<String, String> virtualhostProperties = new HashMap<>();
                    virtualhostProperties.put(IPACL_ALLOW, "127.0.0.0/8,0:0:0:0:0:0:0:1/128,10.*.*.*,172.*.*.*");
                    virtualhostProperties.put(FULLHASH_PROP, "xxxxxxxxxx");
                    virtuahost.setProperties(virtualhostProperties);
                    RuleType ruleType = new RuleType(EnumRuleType.PATH.toString());
                    Pool pool = new Pool("pool_test");
                    Target target = new Target("http://127.0.0.1:8080");
                    Map<String, String> targetProperties = new HashMap<>();
                    targetProperties.put(PROP_HEALTHY.value(), OK.toString());
                    target.setProperties(targetProperties);
                    pool.setTargets(Collections.singleton(target));
                    BalancePolicyType balancePolicyTypeRR = new BalancePolicyType(HostSelectorLookup.ROUNDROBIN.toString());
                    BalancePolicy balancePolicyRR = new BalancePolicy(HostSelectorLookup.ROUNDROBIN.toString(), balancePolicyTypeRR);
                    pool.setBalancePolicy(balancePolicyRR);
                    Rule rule_slash = new Rule("rule_test_slash", ruleType, pool);
                    Map<String, String> ruleProperties = new HashMap<>();
                    ruleProperties.put(RULE_MATCH, "/");
                    ruleProperties.put(RULE_ORDER, Integer.toString(Integer.MAX_VALUE - 1));
                    rule_slash.setProperties(ruleProperties);
                    Rule other_rule = new Rule("other_rule", ruleType, pool);
                    Map<String, String> otherRuleProperties = new HashMap<>();
                    otherRuleProperties.put(RULE_MATCH, "/search");
                    otherRuleProperties.put(RULE_ORDER, "0");
                    other_rule.setProperties(otherRuleProperties);
                    virtuahost.setRules(new HashSet<>(Arrays.asList(rule_slash, other_rule)));
                    ManagerClient.Virtualhosts virtualhostsFromManager = new ManagerClient.Virtualhosts();
                    virtualhostsFromManager.virtualhosts = new VirtualHost[1];
                    virtualhostsFromManager.virtualhosts[0] = virtuahost;
                    callBack.onCompleted(new Gson().toJson(virtualhostsFromManager));
                }
            }

            @Override
            public void post(String url, String etag) {
                logger.info("sending POST to Manager (ignored) with etag " + etag);
            }
        };
    }
}
