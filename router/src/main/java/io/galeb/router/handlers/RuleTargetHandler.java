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
import io.galeb.core.entity.VirtualHost;
import io.galeb.core.enums.EnumRuleType;
import io.galeb.router.ResponseCodeOnError;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class RuleTargetHandler implements HttpHandler {

    public static final String RULE_ORDER  = "order";
    public static final String RULE_MATCH  = "match";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AtomicBoolean firstLoad = new AtomicBoolean(true);
    private final VirtualHost virtualHost;
    private final PathGlobHandler pathGlobHandler;
    private final ApplicationContext context;
    private PoolHandler poolHandler = null;


    public RuleTargetHandler(final VirtualHost virtualHost, final ApplicationContext context) {
        this.context = context;
        Assert.notNull(virtualHost, "[ Virtualhost NOT FOUND ]");
        this.pathGlobHandler = new PathGlobHandler().setDefaultHandler(loadRulesHandler());
        this.virtualHost = virtualHost;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (poolHandler != null) {
            poolHandler.handleRequest(exchange);
        } else {
            pathGlobHandler.handleRequest(exchange);
        }
    }

    public PoolHandler getPoolHandler() {
        return poolHandler;
    }

    public PathGlobHandler getPathGlobHandler() {
        return pathGlobHandler;
    }

    private HttpHandler loadRulesHandler() {
        return new HttpHandler() {

            @Override
            public synchronized void handleRequest(HttpServerExchange exchange) throws Exception {
                if (pathGlobHandler.getPaths().isEmpty()) {
                    loadRules();
                }
                if (poolHandler != null) {
                    poolHandler.handleRequest(exchange);
                    return;
                }
                if (!pathGlobHandler.getPaths().isEmpty()) {
                    if (firstLoad.getAndSet(false)) {
                        pathGlobHandler.handleRequest(exchange);
                    } else {
                        ResponseCodeOnError.RULE_PATH_NOT_FOUND.getHandler().handleRequest(exchange);
                    }
                } else {
                    ResponseCodeOnError.RULES_EMPTY.getHandler().handleRequest(exchange);
                }
            }

            private void loadRules() {
                final Rule ruleSlashOnly;
                if (virtualHost.getRules().size() == 1 &&
                        (ruleSlashOnly = virtualHost.getRules().stream().findAny().orElse(null)) != null &&
                        EnumRuleType.PATH.toString().equals(ruleSlashOnly.getRuleType().getName()) &&
                        ruleSlashOnly.getProperties().get(RULE_MATCH).equals("/")) {
                    poolHandler = new PoolHandler(ruleSlashOnly.getPool(), context);
                    return;
                }

                final Rule ruleDefault = virtualHost.getRuleDefault();
                if (ruleDefault != null) {
                    pathGlobHandler.setDefaultHandler(new PoolHandler(ruleDefault.getPool(), context));
                }

                virtualHost.getRules().forEach(rule -> {
                    String order = Optional.ofNullable(rule.getProperties().get(RULE_ORDER)).orElse(String.valueOf(Integer.MAX_VALUE));
                    String type = rule.getRuleType().getName();
                    Pool pool = rule.getPool();
                    String path = rule.getProperties().get(RULE_MATCH);
                    if (path != null) {
                        logger.info("[" + virtualHost.getName() + "] adding Rule " + rule.getName() + " [order:" + order + ", type:" + type + "]");

                        if (EnumRuleType.PATH.toString().equals(type)) {
                            final PoolHandler poolHandler = new PoolHandler(pool, context);
                            pathGlobHandler.addPath(path, Integer.parseInt(order), poolHandler);
                        }
                    } else {
                        logger.warn("[" + virtualHost.getName() + "] Rule " + rule.getName() + " ignored. properties.match IS NULL");
                    }
                });
            }
        };
    }

}
