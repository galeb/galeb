package io.galeb.router.handlers.builder;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableSortedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.xnio.OptionMap;

import io.galeb.core.entity.Pool;
import io.galeb.core.entity.Rule;
import io.galeb.core.entity.VirtualHost;
import io.galeb.core.enums.EnumRuleType;
import io.galeb.router.configurations.ManagerClientCacheConfiguration.ManagerClientCache;
import io.galeb.router.handlers.PathGlobHandler;
import io.galeb.router.handlers.PathGlobHandler.PathOrdered;
import io.galeb.router.handlers.PoolHandler;
import io.galeb.router.handlers.RequestIDHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.IPAddressAccessControlHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.server.handlers.proxy.ProxyHandler;

public class HandlerBuilder {

    private static final Logger logger = LoggerFactory.getLogger(HandlerBuilder.class);

    public static final String RULE_ORDER = "order";
    public static final String RULE_MATCH = "match";
    public static final String IPACL_ALLOW = "allow";

    private final ProxyBuilder proxyBuilder = new ProxyBuilder();

    public HandlerBuilder() {

    }

    public void build(List<VirtualHost> vhs, final ApplicationContext context, final NameVirtualHostHandler vhHandler, final ManagerClientCache cache) {
        OptionMap options = context.getBean("undertowOptionMap", OptionMap.class);

        vhs.forEach(vh -> {
            logger.info("adding " + vh.getName());
            HttpHandler handler = buildVirtualHost(vh, options);
            vhHandler.addHost(vh.getName(), handler);
            cache.put(vh.getName(), vh);

            vh.getAliases().forEach(aliasName -> {
                logger.info("adding alias: " + aliasName);
                vhHandler.addHost(aliasName, handler);
            });
        });
    }

    public HttpHandler buildVirtualHost(VirtualHost vh, final OptionMap options) {
        if (vh.getRules().size() == 1) {
            PoolHandler ph = buildPoolHandler(vh, options);
            if (ph != null) {
                return new RequestIDHandler(ph);
            }
        }

        HttpHandler pgh = buildPathGlobHandler(vh, options);

        if (vh.getProperties().containsKey(IPACL_ALLOW)) {
            final IPAddressAccessControlHandler ipAddressAccessControlHandler = new IPAddressAccessControlHandler()
                    .setNext(pgh);
            Arrays.asList(vh.getProperties().get(IPACL_ALLOW).split(","))
                    .forEach(ipAddressAccessControlHandler::addAllow);
            ipAddressAccessControlHandler.setDefaultAllow(false);
            return ipAddressAccessControlHandler;
        }
        return pgh;
    }

    public PoolHandler buildPoolHandler(VirtualHost vh, final OptionMap options) {
        final Rule ruleSlashOnly = vh.getRules().stream().findAny().orElse(null);
        if (ruleSlashOnly != null && EnumRuleType.PATH.toString().equals(ruleSlashOnly.getRuleType().getName())
                && ruleSlashOnly.getProperties().get(RULE_MATCH).equals("/")) {
            Pool p = ruleSlashOnly.getPool();
            ProxyHandler proxy = proxyBuilder.buildProxy(p, options);
            return new PoolHandler(ruleSlashOnly.getPool(), proxy);
        }
        return null;
    }

    public HttpHandler buildPathGlobHandler(final VirtualHost virtualHost, final OptionMap options) {
        HttpHandler defaultHandler = null;
        final Rule ruleDefault = virtualHost.getRuleDefault();
        if (ruleDefault != null) {
            Pool p = ruleDefault.getPool();
            ProxyHandler proxy = proxyBuilder.buildProxy(p, options);
            defaultHandler = new PoolHandler(ruleDefault.getPool(), proxy);
        }

        Map<PathOrdered, HttpHandler> allPaths = new LinkedHashMap<PathOrdered, HttpHandler>();
        virtualHost.getRules().forEach(rule -> {
            String orderStr = Optional.ofNullable(rule.getProperties().get(RULE_ORDER))
                    .orElse(String.valueOf(Integer.MAX_VALUE));
            String type = rule.getRuleType().getName();
            Pool pool = rule.getPool();
            String path = rule.getProperties().get(RULE_MATCH);

            if (path == null) {
                logger.warn("[" + virtualHost.getName() + "] Rule " + rule.getName()
                        + " ignored. properties.match IS NULL");
                return;
            }

            if (!EnumRuleType.PATH.toString().equals(type)) {
                return;
            }
            logger.info("[" + virtualHost.getName() + "] adding Rule " + rule.getName() + " [path: " +  path + " order:" + orderStr
                    + ", type:" + type + "]");

            final ProxyHandler proxy = proxyBuilder.buildProxy(pool, options);
            final PoolHandler poolHandler = new PoolHandler(pool, proxy);
            int order = Integer.parseInt(orderStr);
            PathOrdered p;
            if (path.endsWith("/") && !path.contains("*")) {
                p = new PathOrdered(path + "*", order);
            } else {
                p = new PathOrdered(path, order);
            }
            allPaths.put(p, poolHandler);
        });

        ImmutableSortedMap<PathOrdered, HttpHandler> paths = ImmutableSortedMap.<PathOrdered, HttpHandler>naturalOrder()
                .putAll(allPaths).build();

        PathGlobHandler pathGlobHandler = new PathGlobHandler(paths);
        if (defaultHandler != null) {
            pathGlobHandler.setDefaultHandler(defaultHandler);
        }

        return new RequestIDHandler(pathGlobHandler);
    }
}
