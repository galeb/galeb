package io.galeb.router.handlers;

import io.galeb.router.services.ExternalData;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.IPAddressAccessControlHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.zalando.boot.etcd.EtcdNode;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.galeb.router.services.ExternalData.VIRTUALHOSTS_KEY;

public class RuleTargetHandler implements HttpHandler {

    public enum RuleType {
        PATH,
        UNDEF
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AtomicBoolean loaded = new AtomicBoolean(false);
    private final HttpHandler next;
    private final ApplicationContext context;
    private final ExternalData data;
    private String virtualHost;

    public RuleTargetHandler(final ApplicationContext context, final ExternalData externalData) {
        this.context = context;
        this.data = externalData;

        final PathGlobHandler pathGlobHandler = (PathGlobHandler) context.getBean("pathGlobHandler");
        this.next = hasAcl() ? loadAcl() : pathGlobHandler;
        pathGlobHandler.setDefaultHandler(loadRulesHandler(next));
    }

    public RuleTargetHandler setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
        return this;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        next.handleRequest(exchange);
    }

    private HttpHandler loadRulesHandler(HttpHandler next) {
        return new HttpHandler() {

            final PathGlobHandler pathGlobHandler = next instanceof PathGlobHandler ? (PathGlobHandler) next : (PathGlobHandler) ((IPAddressAccessControlHandler) next).getNext();

            @Override
            public synchronized void handleRequest(HttpServerExchange exchange) throws Exception {
                if (!loaded.get()) {
                    loadRules(virtualHost);
                    next.handleRequest(exchange);
                } else {
                    ResponseCodeHandler.HANDLE_500.handleRequest(exchange);
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
                return Integer.valueOf(data.node(ruleKey + "/order", ExternalData.GenericNode.ZERO).getValue());
            }

            private void loadRules(String virtualHost) {
                EtcdNode rulesNode = data.node(VIRTUALHOSTS_KEY + "/" + virtualHost + "/rules");
                if (rulesNode.getKey() != null) {
                    final List<EtcdNode> rulesRegistered = data.listFrom(rulesNode);
                    if (!rulesRegistered.isEmpty()) {
                        for (EtcdNode keyComplete : rulesRegistered) {
                            if (keyComplete.isDir()) {
                                String ruleKey = keyComplete.getKey();
                                Integer order = extractRuleOrder(ruleKey);
                                String type = extractRuleType(ruleKey);
                                String rule = extractRule(ruleKey);
                                String ruleDecoded = extractRuleDecoded(rule);
                                String ruleTarget = extractRuleTarget(ruleKey);

                                logger.info("add rule " + ruleDecoded + " [order:" + order + ", type:" + type + "]");

                                if (RuleType.valueOf(type) == RuleType.PATH) {
                                    final PoolHandler poolHandler = ((PoolHandler) context.getBean("poolHandler")).setPoolName(ruleTarget);
                                    pathGlobHandler.addPath(ruleDecoded, order, poolHandler);
                                }
                            }
                        }
                        loaded.set(true);
                    }
                }
            }
        };
    }

    private boolean hasAcl() {
        return data.exist(VIRTUALHOSTS_KEY + "/" + virtualHost + "allow");
    }

    private HttpHandler loadAcl() {
        final IPAddressAccessControlHandler ipAddressAccessControlHandler = new IPAddressAccessControlHandler().setNext(next);
        Arrays.asList(data.node(VIRTUALHOSTS_KEY + "/" + virtualHost + "allow").getValue().split(","))
                .forEach(ipAddressAccessControlHandler::addAllow);
        return ipAddressAccessControlHandler;
    }
}
