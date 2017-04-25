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

import io.galeb.router.ResponseCodeOnError;
import io.galeb.router.services.ExternalDataService;
import io.galeb.router.kv.ExternalData;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.IPAddressAccessControlHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static io.galeb.router.services.ExternalDataService.VIRTUALHOSTS_KEY;

public class RuleTargetHandler implements HttpHandler {

    public enum RuleType {
        PATH,
        UNDEF
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ExternalDataService data;
    private final String virtualHost;

    private HttpHandler next = null;

    public RuleTargetHandler(final ExternalDataService externalData, final String virtualHost) {
        this.data = externalData;
        this.virtualHost = virtualHost;
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
                    loadRules(virtualHost);
                }
                if (!pathGlobHandler.getPaths().isEmpty()) {
                    next.handleRequest(exchange);
                } else {
                    ResponseCodeOnError.RULES_EMPTY.getHandler().handleRequest(exchange);
                }
            }

            private String extractRule(String ruleKey) {
                int ruleFromIndex = ruleKey.lastIndexOf("/");
                return ruleKey.substring(ruleFromIndex + 1, ruleKey.length());
            }

            private String extractRuleDecoded(String rule) {
                return new String(Base64.getDecoder().decode(rule.getBytes(Charset.defaultCharset())), Charset.defaultCharset()).trim();
            }

            private String extractRuleType(String ruleKey) {
                return Optional.ofNullable(data.node(ruleKey + "/type").getValue()).orElse(RuleType.PATH.toString());
            }

            private String extractRuleTarget(String ruleKey) {
                return Optional.ofNullable(data.node(ruleKey + "/target").getValue()).orElse("");
            }

            private Integer extractRuleOrder(String ruleKey) {
                return Integer.valueOf(data.node(ruleKey + "/order", ExternalData.Generic.ZERO).getValue());
            }

            private void loadRules(String virtualHost) {
                ExternalData rulesNode = data.node(VIRTUALHOSTS_KEY + "/" + virtualHost + "/rules");
                if (rulesNode.getKey() != null) {
                    final List<ExternalData> rulesRegistered = data.listFrom(rulesNode);
                    if (!rulesRegistered.isEmpty()) {
                        for (ExternalData keyComplete : rulesRegistered) {
                            if (keyComplete.isDir()) {
                                String ruleKey = keyComplete.getKey();
                                Integer order = extractRuleOrder(ruleKey);
                                String type = extractRuleType(ruleKey);
                                String rule = extractRule(ruleKey);
                                String ruleDecoded = extractRuleDecoded(rule);
                                String ruleTarget = extractRuleTarget(ruleKey);

                                logger.info("add rule " + ruleDecoded + " [order:" + order + ", type:" + type + "]");

                                if (RuleType.valueOf(type) == RuleType.PATH) {
                                    final PoolHandler poolHandler = new PoolHandler(data).setPoolName(ruleTarget);
                                    pathGlobHandler.addPath(ruleDecoded, order, poolHandler);
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private boolean hasAcl() {
        return data.exist(VIRTUALHOSTS_KEY + "/" + virtualHost + "/allow");
    }

    private HttpHandler loadAcl(PathGlobHandler pathGlobHandler) {
        final IPAddressAccessControlHandler ipAddressAccessControlHandler = new IPAddressAccessControlHandler().setNext(pathGlobHandler);
        Arrays.asList(data.node(VIRTUALHOSTS_KEY + "/" + virtualHost + "/allow").getValue().split(","))
                .forEach(ipAddressAccessControlHandler::addAllow);
        ipAddressAccessControlHandler.setDefaultAllow(false);
        return ipAddressAccessControlHandler;
    }
}
