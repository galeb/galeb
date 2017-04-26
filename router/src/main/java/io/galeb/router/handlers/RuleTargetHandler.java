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

package io.galeb.router.handlers;

import io.galeb.core.entity.Pool;
import io.galeb.core.entity.Rule;
import io.galeb.core.entity.RuleType;
import io.galeb.core.entity.VirtualHost;
import io.galeb.core.rest.ManagerClient;
import io.galeb.core.rest.EnumRuleType;
import io.galeb.router.ResponseCodeOnError;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.IPAddressAccessControlHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.*;

public class RuleTargetHandler implements HttpHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ManagerClient managerClient;
    private final VirtualHost virtualHost;

    private HttpHandler next = null;

    public RuleTargetHandler(final ManagerClient managerClient, final String virtualHostName) {
        this.managerClient = managerClient;
        this.virtualHost = managerClient.getVirtualhostByName(virtualHostName);
        Assert.notNull(virtualHost, "[ Virtualhost NOT FOUND ]");
        final PathGlobHandler pathGlobHandler = new PathGlobHandler();
        this.next = hasAcl() ? loadAcl(pathGlobHandler) : pathGlobHandler;
        pathGlobHandler.setDefaultHandler(loadRulesHandler(next));
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        next.handleRequest(exchange);
    }

    public HttpHandler getNext() {
        return next;
    }

    private HttpHandler loadRulesHandler(HttpHandler next) {
        return new HttpHandler() {

            final PathGlobHandler pathGlobHandler = next instanceof PathGlobHandler ? (PathGlobHandler) next : (PathGlobHandler) ((IPAddressAccessControlHandler) next).getNext();

            @Override
            public synchronized void handleRequest(HttpServerExchange exchange) throws Exception {
                if (pathGlobHandler.getPaths().isEmpty()) {
                    loadRules();
                }
                if (!pathGlobHandler.getPaths().isEmpty()) {
                    next.handleRequest(exchange);
                } else {
                    ResponseCodeOnError.RULES_EMPTY.getHandler().handleRequest(exchange);
                }
            }

            private String extractRuleType(Rule rule) {
                RuleType ruleType = rule.getRuleType();
                return ruleType.getName();
            }

            private Long extractPoolId(Rule rule) {
                Pool pool = rule.getPool();
                return pool.getId();
            }

            private void loadRules() {
                Set<Rule> rules = managerClient.getRulesByVirtualhost(virtualHost);
                if (!rules.isEmpty()) {
                    for (Rule rule : rules) {
                        Integer order = rule.getRuleOrder();
                        String type = extractRuleType(rule);
                        Long poolId = extractPoolId(rule);

                        logger.info("add rule " + rule.getName() + " [order:" + order + ", type:" + type + "]");

                        if (EnumRuleType.valueOf(type) == EnumRuleType.PATH) {
                            final PoolHandler poolHandler = new PoolHandler(managerClient).setPooId(poolId);
                            pathGlobHandler.addPath(rule.getName(), order, poolHandler);
                        }

                    }

                }
            }
        };
    }

    private boolean hasAcl() {
        return virtualHost.getProperties().containsKey("allow");
    }

    private HttpHandler loadAcl(PathGlobHandler pathGlobHandler) {
        final IPAddressAccessControlHandler ipAddressAccessControlHandler = new IPAddressAccessControlHandler().setNext(pathGlobHandler);

        Arrays.asList(virtualHost.getProperties().get("allow").split(","))
                .forEach(ipAddressAccessControlHandler::addAllow);
        ipAddressAccessControlHandler.setDefaultAllow(false);
        return ipAddressAccessControlHandler;
    }
}
